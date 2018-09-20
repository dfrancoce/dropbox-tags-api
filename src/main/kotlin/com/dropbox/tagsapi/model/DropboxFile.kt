package com.dropbox.tagsapi.model

import org.apache.solr.client.solrj.beans.Field
import org.springframework.data.annotation.Id
import org.springframework.data.solr.core.mapping.Indexed
import org.springframework.data.solr.core.mapping.SolrDocument
import java.util.*

/**
 * This class represents a dropbox file in solr
 */
@SolrDocument(solrCoreName = "dropboxFile")
data class DropboxFile(@Id @Field val id: String, @Field val name: String, @Field val path: String, @Field @Indexed val tags: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DropboxFile

        if (id != other.id) return false
        if (name != other.name) return false
        if (path != other.path) return false
        if (!Arrays.equals(tags, other.tags)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + Arrays.hashCode(tags)
        return result
    }
}