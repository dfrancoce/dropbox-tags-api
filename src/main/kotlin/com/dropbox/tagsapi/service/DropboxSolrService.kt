package com.dropbox.tagsapi.service

import com.dropbox.tagsapi.model.DropboxFile
import com.dropbox.tagsapi.repository.DropboxSolrRepository
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

/**
 * This class is responsible of the business logic related to the search of dropbox files in solr
 */
@Service
class DropboxSolrService(private val dropboxService: DropboxService, private val dropboxSolrRepository: DropboxSolrRepository) {

    /**
     * Gets all the files from dropbox and indexes them
     */
    @PostConstruct
    fun index() {
        val dropboxFiles = dropboxService.getFilesFromDropbox()
        dropboxSolrRepository.saveAll(dropboxFiles)
    }
}