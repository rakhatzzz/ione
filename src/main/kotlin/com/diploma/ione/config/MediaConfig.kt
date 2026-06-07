package com.diploma.ione.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class MediaConfig(
    @Value("\${app.media.dir}") private val mediaDir: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val file = File(mediaDir)
        val locationPath = file.toURI().toString()
        registry.addResourceHandler("/media/**")
            .addResourceLocations(locationPath)
            .setCachePeriod(3600)
    }
}