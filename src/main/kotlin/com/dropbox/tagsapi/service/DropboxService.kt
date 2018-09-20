package com.dropbox.tagsapi.service

import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.tagsapi.client.DropboxClient
import com.dropbox.tagsapi.model.DropboxFile
import org.springframework.stereotype.Service

/**
 * This class is responsible of interacting with dropbox to make operations with the files stored
 * there
 */
@Service
class DropboxService {
    private val dropboxFiles: MutableList<DropboxFile> = mutableListOf()

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
                val dropboxFile = DropboxFile(id = metadata.id, name = metadata.name, path = metadata.pathLower, tags = arrayOf(""))
                dropboxFiles.add(dropboxFile)
            }

            if (result.hasMore) {
                iterateDropboxFoldersAndFiles(client, client.files().listFolderContinue(result.cursor))
            }
        }
    }
}