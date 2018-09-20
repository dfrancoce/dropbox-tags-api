package com.dropbox.tagsapi.repository

import com.dropbox.tagsapi.model.DropboxFile
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.solr.repository.SolrCrudRepository

@RepositoryRestResource(collectionResourceRel = "files", path = "files")
interface DropboxSolrRepository : SolrCrudRepository<DropboxFile, String>