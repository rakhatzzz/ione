package com.diploma.ione.web

import com.diploma.ione.repo.CourseRepo
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.SchoolRepo
import com.diploma.ione.repo.TeacherRepo
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

data class PublicSchoolDto(val id: Long, val name: String)

data class PublicTeacherDto(val id: Long, val fullName: String)

data class PublicClassDto(val className: String)

@RestController
@RequestMapping("/api/public")
class PublicContentController(
    private val courseRepo: CourseRepo,
    private val lessonRepo: LessonRepo,
    private val schoolRepo: SchoolRepo,
    private val teacherRepo: TeacherRepo
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

    @GetMapping("/schools")
    fun getSchools(): List<PublicSchoolDto> =
        schoolRepo.findAll().map {
            PublicSchoolDto(
                id = it.id!!,
                name = it.name
            )
        }

    @GetMapping("/schools/{schoolId}/teachers")
    fun getTeachersBySchool(@PathVariable schoolId: Long): List<PublicTeacherDto> {
        schoolRepo.findById(schoolId).orElseThrow { RuntimeException("School not found") }
        return teacherRepo.findAll()
            .filter { it.school.id == schoolId }
            .map {
                PublicTeacherDto(
                    id = it.id!!,
                    fullName = it.user.fullName
                )
            }
    }

    @GetMapping("/schools/{schoolId}/classes")
    fun getClassesBySchool(@PathVariable schoolId: Long): List<PublicClassDto> {
        schoolRepo.findById(schoolId).orElseThrow { RuntimeException("School not found") }
        return teacherRepo.findAll()
            .filter { it.school.id == schoolId }
            .mapNotNull { it.homeroomClass?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .map { PublicClassDto(className = it) }
    }
}