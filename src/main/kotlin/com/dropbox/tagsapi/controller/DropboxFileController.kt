package com.dropbox.tagsapi.controller

import com.dropbox.tagsapi.repository.DropboxSolrRepository
import com.dropbox.tagsapi.service.DropboxService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api")
class DropboxFileController(private val dropboxSolrRepository: DropboxSolrRepository, private val dropboxService: DropboxService) {

    @RequestMapping(value = ["/zip"], produces = ["application/zip"])
    fun getZipFiles(response: HttpServletResponse, @RequestParam(value = "tags", required = false) tags: List<String>) {
        response.status = HttpServletResponse.SC_OK;
        response.addHeader("Content-Disposition", "attachment; filename=\"files.zip\"")

        val dropboxFilesByTags = dropboxSolrRepository.findByTagsIn(tags, Pageable.unpaged())
        dropboxService.downloadFileFromDropbox(dropboxFilesByTags.content)
    }
}