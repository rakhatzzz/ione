package com.diploma.ione.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Получаем абсолютный путь к нашей папке media
        val mediaPath = File("media").absolutePath
        
        // Говорим Spring раздавать файлы из этой папки по ссылке /media/**
        registry.addResourceHandler("/media/**")
            .addResourceLocations("file:$mediaPath/")
    }
}