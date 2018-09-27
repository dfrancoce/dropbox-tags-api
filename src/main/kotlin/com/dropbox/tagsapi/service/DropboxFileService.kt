package com.dropbox.tagsapi.service

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.tagsapi.client.DropboxClient
import com.dropbox.tagsapi.config.ApiProperties
import com.dropbox.tagsapi.model.DropboxFile
import com.dropbox.tagsapi.repository.DropboxSolrRepository
import org.apache.commons.io.IOUtils
import org.springframework.data.solr.core.query.SolrPageRequest
import org.springframework.stereotype.Service
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.annotation.PostConstruct

/**
 * This class is responsible of interacting with dropbox and solr to provide the operations for the API
 *
 */
@Service
class DropboxFileService(apiProperties: ApiProperties, private val dropboxSolrRepository: DropboxSolrRepository) {
    private val dropboxFiles: MutableList<DropboxFile> = mutableListOf()
    private val client = DropboxClient.getClient(apiProperties.dropboxAccessToken)
    private val limit = apiProperties.zipSize.toLong() * Math.pow(1024.0, 2.0)

    /**
     * Gets all the files from dropbox and indexes them
     */
    @PostConstruct
    fun index() {
        val dropboxFiles = getFilesFromDropbox()
        dropboxFiles.filter { file -> !dropboxSolrRepository.existsById(file.id) }.forEach { file -> dropboxSolrRepository.save(file) }
    }

    /**
     * Returns the all the files stored in the Dropbox of the client
     */
    private fun getFilesFromDropbox(): List<DropboxFile> {
        val builder = client.files().listFolderBuilder("")
        val result = builder.withRecursive(true).start()

        iterateDropboxFoldersAndFiles(result)
        return dropboxFiles
    }

    private fun iterateDropboxFoldersAndFiles(result: ListFolderResult) {
        for (metadata in result.entries) {
            if (metadata is FileMetadata) {
                val dropboxFile = DropboxFile(id = metadata.id.split(":")[1], name = metadata.name,
                        path = metadata.pathLower, size = metadata.size, tags = null)
                dropboxFiles.add(dropboxFile)
            }

            if (result.hasMore) {
                iterateDropboxFoldersAndFiles(client.files().listFolderContinue(result.cursor))
            }
        }
    }

    /**
     * Returns a zip file containing all the dropbox files passed by parameter
     */
    fun downloadFileFromDropbox(tags: List<String>, outputStream: OutputStream): ZipOutputStream? {
        val dropboxFiles = dropboxSolrRepository.findByTagsIn(tags, SolrPageRequest(0, Integer.MAX_VALUE))
        val totalSize = dropboxFiles.map { dropboxFile -> dropboxFile.size }.sum()

        if (totalSize > limit) {
            throw Exception("The total size of the files requested is greater than the limit configured")
        }

        val zipOutputStream = ZipOutputStream(outputStream)
        for (dropboxFile in dropboxFiles) {
            val downloadedFile = client.files().download(dropboxFile.path)
            val zipEntry = ZipEntry(dropboxFile.name)
            zipOutputStream.putNextEntry(zipEntry)

            val downloadedFileInputStream = downloadedFile.inputStream
            IOUtils.copy(downloadedFileInputStream, zipOutputStream)
            downloadedFileInputStream.close()
            zipOutputStream.closeEntry()
        }

        zipOutputStream.close()
        return zipOutputStream
    }
}