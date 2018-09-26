package com.dropbox.tagsapi.client

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2

/**
 * This object represents the client used to connect with dropbox
 */
object DropboxClient {
    fun getClient(dropboxAccessToken: String): DbxClientV2 {
        val config = DbxRequestConfig.newBuilder("dropbox-tags-api").build()
        return DbxClientV2(config, dropboxAccessToken)
    }
}