package com.dropbox.tagsapi.controller

import com.dropbox.tagsapi.repository.DropboxSolrRepository
import com.dropbox.tagsapi.service.DropboxService
import org.springframework.data.solr.core.query.SolrPageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

/**
 * This class provides a custom endpoint to download a bunch of files (result of a search by tags in solr) as
 * a zip file
 */
@RestController
@RequestMapping("/api")
class DropboxFileController(private val dropboxSolrRepository: DropboxSolrRepository, private val dropboxService: DropboxService) {

    @RequestMapping(value = ["/zip"], produces = ["application/zip"])
    @ResponseStatus(HttpStatus.OK)
    fun getZip(response: HttpServletResponse, @RequestParam(value = "tags", required = false) tags: List<String>) {
        response.addHeader("Content-Disposition", "attachment; filename=\"files.zip\"")

        val dropboxFilesByTags = dropboxSolrRepository.findByTagsIn(tags, SolrPageRequest(0, Integer.MAX_VALUE))
        dropboxService.downloadFileFromDropbox(dropboxFilesByTags.content, response.outputStream)
    }
}