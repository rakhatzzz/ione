package com.diploma.ione.web

import com.diploma.ione.repo.CourseRepo
import com.diploma.ione.repo.LessonRepo
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public")
class PublicContentController(
    private val courseRepo: CourseRepo,
    private val lessonRepo: LessonRepo
) {
    @GetMapping("/courses")
    fun courses(): List<CourseDto> =
        courseRepo.findAll().map {
            CourseDto(
                id = it.id!!,
                title = it.title,
                description = it.description,
                ageGroup = it.ageGroup
            )
        }

    @GetMapping("/courses/{courseId}/lessons")
    fun lessons(@PathVariable courseId: Long, request: HttpServletRequest): List<LessonDto> {
        val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}"
        return lessonRepo.findAllByCourseIdOrderByOrderNumberAsc(courseId).map { l ->
            LessonDto(
                id = l.id!!,
                courseId = courseId,
                title = l.title,
                orderNumber = l.orderNumber,
                videoUrl = l.videoPath?.let { "$baseUrl/media/$it" },
                textContent = l.textContent
            )
        }
    }

    @GetMapping("/lessons/{lessonId}")
    fun lesson(@PathVariable lessonId: Long, request: HttpServletRequest): LessonDto {
        val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}"
        val l = lessonRepo.findById(lessonId).orElseThrow { RuntimeException("Lesson not found") }
        return LessonDto(
            id = l.id!!,
            courseId = l.course.id!!,
            title = l.title,
            orderNumber = l.orderNumber,
            videoUrl = l.videoPath?.let { "$baseUrl/media/$it" },
            textContent = l.textContent
        )
    }
}