package com.diploma.ione.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/admin/media")
class AdminMediaController {

    @PostMapping("/upload")
    fun uploadMedia(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        // Здесь логика сохранения файла на диск/в облако
        val randomFilename = UUID.randomUUID().toString() + "_" + (file.originalFilename ?: "uploaded.jpg")
        val fileUrl = "/media/$randomFilename"
        return ResponseEntity.ok(mapOf("url" to fileUrl))
    }
}