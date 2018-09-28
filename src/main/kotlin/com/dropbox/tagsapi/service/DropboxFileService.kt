package com.dropbox.tagsapi.service

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.tagsapi.client.DropboxClient
import com.dropbox.tagsapi.config.ApiProperties
import com.dropbox.tagsapi.model.DropboxFile
import com.dropbox.tagsapi.repository.DropboxSolrRepository
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.solr.core.query.SolrPageRequest
import org.springframework.stereotype.Service
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.annotation.PostConstruct

/**
 * This class contains the business logic responsible for interacting with Dropbox and Solr to make the operations
 * requested
 *
 */
@Service
class DropboxFileService(apiProperties: ApiProperties, private val dropboxSolrRepository: DropboxSolrRepository) {
    private val logger: Logger = LoggerFactory.getLogger(DropboxFileService::class.java)
    private val dropboxFiles: MutableList<DropboxFile> = mutableListOf()
    private val client = DropboxClient.getClient(apiProperties.dropboxAccessToken)
    private val limit = apiProperties.zipSize.toLong() * Math.pow(1024.0, 2.0)

    /**
     * Gets all the files from dropbox and indexes them
     */
    @PostConstruct
    fun index() {
        logger.debug("DropboxFileService - index - start")

        val dropboxFiles = getFilesFromDropbox()
        dropboxFiles.filter { file -> !dropboxSolrRepository.existsById(file.id) }.forEach { file -> dropboxSolrRepository.save(file) }
    }

    /**
     * Returns the all the files stored in the Dropbox of the client
     */
    private fun getFilesFromDropbox(): List<DropboxFile> {
        logger.debug("DropboxFileService - getFilesFromDropbox - start")

        val builder = client.files().listFolderBuilder("")
        val result = builder.withRecursive(true).start()
        iterateDropboxFoldersAndFiles(result)

        return dropboxFiles
    }

    /**
     * Iterates through all the elements in dropbox and builds the DropboxFile objects that will be indexed in Solr
     */
    private fun iterateDropboxFoldersAndFiles(result: ListFolderResult) {
        logger.debug("DropboxFileService - iterateDropboxFoldersAndFiles - start")

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
    fun downloadZipFileFromDropbox(tags: List<String>, outputStream: OutputStream): ZipOutputStream? {
        logger.debug("DropboxFileService - downloadZipFileFromDropbox - start")

        val dropboxFiles = dropboxSolrRepository.findByTagsIn(tags, SolrPageRequest(0, Integer.MAX_VALUE)).content
        val totalSize = dropboxFiles.asSequence().map { dropboxFile -> dropboxFile.size }.sum()
        logger.debug("DropboxFileService - downloadZipFileFromDropbox - totalSize = {}", totalSize)
        if (totalSize > limit) {
            logger.error("DropboxFileService - downloadZipFileFromDropbox - The total size of the files requested is greater than the limit configured")
            return null
        }

        val zipOutputStream = ZipOutputStream(outputStream)
        for (dropboxFile in dropboxFiles) {
            val downloadedFile = client.files().download(dropboxFile.path)
            val downloadedFileInputStream = downloadedFile.inputStream
            val zipEntry = ZipEntry(dropboxFile.name)

            zipOutputStream.putNextEntry(zipEntry)
            IOUtils.copy(downloadedFileInputStream, zipOutputStream)
            downloadedFileInputStream.close()
            zipOutputStream.closeEntry()
        }

        zipOutputStream.close()
        return zipOutputStream
    }
}