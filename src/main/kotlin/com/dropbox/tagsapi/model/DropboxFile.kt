package com.dropbox.tagsapi.model

import org.apache.solr.client.solrj.beans.Field
import org.springframework.data.annotation.Id
import org.springframework.data.solr.core.mapping.Indexed
import org.springframework.data.solr.core.mapping.SolrDocument

/**
 * This class represents a dropbox file in solr
 */
@SolrDocument(solrCoreName = "dropboxFile")
data class DropboxFile(@Id @Field val id: String, @Field val name: String, @Field val path: String, @Field @Indexed val tags: MutableList<String>?)