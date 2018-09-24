package com.dropbox.tagsapi.service

import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.tagsapi.client.DropboxClient
import com.dropbox.tagsapi.model.DropboxFile
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * This class is responsible of interacting with dropbox to make operations with the files stored
 * there
 */
@Service
class DropboxService {
    private val dropboxFiles: MutableList<DropboxFile> = mutableListOf()

    fun downloadFileFromDropbox(dropboxFiles: List<DropboxFile>): ZipOutputStream {
        val client = DropboxClient.getClient()
        val filesOutputStream = FileOutputStream("files.zip")
        val zipOutputStream = ZipOutputStream(filesOutputStream)

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

    /**
     * Returns the all the files stored in the Dropbox of the client
     */
    fun getFilesFromDropbox(): List<DropboxFile> {
        val client = DropboxClient.getClient()
        val builder = client.files().listFolderBuilder("")
        val result = builder.withRecursive(true).start()

        iterateDropboxFoldersAndFiles(client, result)
        return dropboxFiles
    }

    private fun iterateDropboxFoldersAndFiles(client: DbxClientV2, result: ListFolderResult) {
        for (metadata in result.entries) {
            if (metadata is FileMetadata) {
                val dropboxFile = DropboxFile(id = metadata.id.split(":")[1], name = metadata.name, path = metadata.pathLower, tags = null)
                dropboxFiles.add(dropboxFile)
            }

            if (result.hasMore) {
                iterateDropboxFoldersAndFiles(client, client.files().listFolderContinue(result.cursor))
            }
        }
    }
}