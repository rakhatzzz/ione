package com.diploma.ione.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class MediaConfig(
    @Value("\${app.media.dir}") private val mediaDir: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val location = Paths.get(mediaDir).toUri().toString()
        registry.addResourceHandler("/media/**")
            .addResourceLocations(location)
    }
}