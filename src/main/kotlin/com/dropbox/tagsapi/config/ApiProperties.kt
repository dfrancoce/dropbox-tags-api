package com.dropbox.tagsapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import javax.validation.constraints.NotNull

@Component
@ConfigurationProperties(prefix = "api")
class ApiProperties {
    @NotNull lateinit var dropboxAccessToken: String
    @NotNull lateinit var zipSize: String
}