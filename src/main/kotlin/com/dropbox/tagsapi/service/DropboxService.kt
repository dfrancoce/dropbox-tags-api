package com.dropbox.tagsapi.service

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.tagsapi.client.DropboxClient
import com.dropbox.tagsapi.config.ApiProperties
import com.dropbox.tagsapi.model.DropboxFile
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
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
    private val limit = apiProperties.zipSize.toLong()

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
                val dropboxFile = DropboxFile(id = metadata.id.split(":")[1], name = metadata.name, path = metadata.pathLower, tags = null)
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
        var currentSize = 0L

        for (dropboxFile in dropboxFiles) {
            val downloadedFile = client.files().download(dropboxFile.path)

            val zipEntry = ZipEntry(dropboxFile.name)
            if (currentSize + zipEntry.compressedSize > limit) {
                break
            }

            zipOutputStream.putNextEntry(zipEntry)
            val downloadedFileInputStream = downloadedFile.inputStream
            IOUtils.copy(downloadedFileInputStream, zipOutputStream)
            currentSize += zipEntry.compressedSize
            downloadedFileInputStream.close()
            zipOutputStream.closeEntry()
        }

        zipOutputStream.close()
        return zipOutputStream
    }
}