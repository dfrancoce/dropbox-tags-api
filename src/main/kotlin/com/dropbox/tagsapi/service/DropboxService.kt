package com.dropbox.tagsapi.service

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.tagsapi.client.DropboxClient
import com.dropbox.tagsapi.config.ApiProperties
import com.dropbox.tagsapi.model.DropboxFile
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * This class is responsible of interacting with dropbox to make operations with its files
 *
 */
@Service
class DropboxService(apiProperties: ApiProperties) {
    private val dropboxFiles: MutableList<DropboxFile> = mutableListOf()
    private val client = DropboxClient.getClient(apiProperties.dropboxAccessToken)
    private val limit = apiProperties.zipSize.toLong() * Math.pow(1024.0, 2.0)

    /**
     * Returns the all the files stored in the Dropbox of the client
     */
    fun getFilesFromDropbox(): List<DropboxFile> {
        val builder = client.files().listFolderBuilder("")
        val result = builder.withRecursive(true).start()

        iterateDropboxFoldersAndFiles(result)
        return dropboxFiles
    }

    private fun iterateDropboxFoldersAndFiles(result: ListFolderResult) {
        for (metadata in result.entries) {
            if (metadata is FileMetadata) {
                val dropboxFile = DropboxFile(id = metadata.id.split(":")[1], name = metadata.name, path = metadata.pathLower, size = metadata.size, tags = null)
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
    fun downloadFileFromDropbox(dropboxFiles: List<DropboxFile>, outputStream: OutputStream): ZipOutputStream? {
        val zipOutputStream = ZipOutputStream(outputStream)
        val totalSize = dropboxFiles.asSequence().map { dropboxFile -> dropboxFile.size }.sum()

        if (totalSize > limit) {
            throw IOException("The total size of the files requested is greater than the limit configured")
        }

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