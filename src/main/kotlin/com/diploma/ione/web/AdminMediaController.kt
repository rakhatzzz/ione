package com.diploma.ione.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID

@RestController
@RequestMapping("/api/admin/media")
class AdminMediaController {

    // Создаем базовую папку media в корне бэкенд-проекта
    private val baseMediaDir = File("media")

    init {
        // При старте сервера автоматически создаем папки, если их еще нет
        File(baseMediaDir, "photos").mkdirs()
        File(baseMediaDir, "videos").mkdirs()
    }

    @PostMapping("/upload")
    fun uploadMedia(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Файл пустой"))
        }

        val originalFilename = file.originalFilename ?: "file.jpg"
        // Проверяем, видео это или фото (по расширению)
        val isVideo = originalFilename.lowercase().matches(Regex(".*\\.(mp4|avi|mov|mkv|webm)$"))
        val subFolder = if (isVideo) "videos" else "photos"
        
        val randomFilename = UUID.randomUUID().toString() + "_" + originalFilename
        val targetFile = File(baseMediaDir, "$subFolder/$randomFilename")
        file.transferTo(targetFile.absoluteFile) // Физически сохраняем на диск

        val fileUrl = "/media/$subFolder/$randomFilename"
        return ResponseEntity.ok(mapOf("url" to fileUrl))
    }
}