package com.dropbox.tagsapi.controller


import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 * This class handles the API exceptions
 */
@RestController
@RequestMapping("/error")
class DropboxFileErrorController(errorAttributes: ErrorAttributes?) : AbstractErrorController(errorAttributes) {

    override fun getErrorPath(): String {
        return "/error"
    }

    @RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun handleError(request: HttpServletRequest) : Map<String, Any> {
        return super.getErrorAttributes(request, true)
    }
}