package com.dropbox.tagsapi.repository

import com.dropbox.tagsapi.model.DropboxFile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.solr.repository.SolrCrudRepository

/**
 * Class responsible of the operations against Solr
 */
@RepositoryRestResource(collectionResourceRel = "files", path = "files")
interface DropboxSolrRepository : SolrCrudRepository<DropboxFile, String> {
    fun findByTagsIn(tags: Collection<String>, page: Pageable): Page<DropboxFile>
}