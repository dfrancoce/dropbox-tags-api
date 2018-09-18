package com.dropbox.tagsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TagsApiApplication

fun main(args: Array<String>) {
    runApplication<TagsApiApplication>(*args)
}
