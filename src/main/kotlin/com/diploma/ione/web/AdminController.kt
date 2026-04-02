package com.diploma.ione.web

import com.diploma.ione.domain.Course
import com.diploma.ione.domain.Lesson
import com.diploma.ione.repo.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminDashboardDto(
        val schools: List<AdminSchoolDto>,
        val courses: List<AdminCourseDto>,
        val tests: List<AdminTestDto>,
        val scenarios: List<AdminScenarioDto>
)

data class AdminSchoolDto(val id: Long, val name: String, val teachers: List<AdminTeacherDto>)

data class AdminTeacherDto(val id: Long, val fullName: String, val students: List<AdminStudentDto>)

data class AdminStudentDto(val id: Long, val fullName: String, val className: String?)

data class AdminCourseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val ageGroup: String?,
    val lessons: List<AdminLessonDto>
)

data class AdminLessonDto(
    val id: Long,
    val title: String,
    val orderNumber: Int,
    val videoPath: String?,
    val textContent: String?
)

data class AdminTestDto(val id: Long, val title: String)

data class AdminScenarioDto(val id: Long, val title: String)

data class CreateCourseRequest(val title: String, val description: String?, val ageGroup: String?)

data class UpdateCourseRequest(val title: String?, val description: String?, val ageGroup: String?)

data class CreateLessonRequest(
    val courseId: Long,
    val title: String,
    val videoPath: String?,
    val textContent: String?,
    val orderNumber: Int = 1
)

data class UpdateLessonRequest(
    val title: String?,
    val videoPath: String?,
    val textContent: String?,
    val orderNumber: Int?
)

@RestController
@RequestMapping("/api/admin")
class AdminController(
        private val schoolRepo: SchoolRepo,
        private val teacherRepo: TeacherRepo,
        private val studentRepo: StudentRepo,
        private val courseRepo: CourseRepo,
        private val lessonRepo: LessonRepo,
        private val testRepo: PsychologicalTestRepo,
        private val scenarioRepo: ScenarioRepo
) {
    @GetMapping("/dashboard")
    fun getDashboardData(): AdminDashboardDto {
        val schools = schoolRepo.findAll()
        val teachers = teacherRepo.findAll()
        val students = studentRepo.findAll()

        // Map teachers by school
        val teachersBySchool = teachers.groupBy { it.school.id }
        // Map students by teacher
        val studentsByTeacher = students.groupBy { it.teacher.id }

        val schoolDtos =
                schools.map { school ->
                    val schoolTeachers = teachersBySchool[school.id] ?: emptyList()
                    val teacherDtos =
                            schoolTeachers.map { teacher ->
                                val teacherStudents = studentsByTeacher[teacher.id] ?: emptyList()
                                val studentDtos =
                                        teacherStudents.map { student ->
                                            AdminStudentDto(
                                                    id = student.id!!,
                                                    fullName = student.user.fullName,
                                                    className = student.className
                                            )
                                        }
                                AdminTeacherDto(
                                        id = teacher.id!!,
                                        fullName = teacher.user.fullName,
                                        students = studentDtos
                                )
                            }
                    AdminSchoolDto(id = school.id!!, name = school.name, teachers = teacherDtos)
                }

        val allLessons = lessonRepo.findAll().groupBy { it.course.id }
        val courses = courseRepo.findAll().map { course ->
            val nestedLessons = (allLessons[course.id] ?: emptyList())
                .sortedBy { it.orderNumber }
                .map {
                    AdminLessonDto(
                        id = it.id!!,
                        title = it.title,
                        orderNumber = it.orderNumber,
                        videoPath = it.videoPath,
                        textContent = it.textContent
                    )
                }
            AdminCourseDto(course.id!!, course.title, course.description, course.ageGroup, nestedLessons)
        }
        val tests = testRepo.findAll().map { AdminTestDto(it.id!!, it.title) }
        val scenarios =
                scenarioRepo.findAll().map {
                    AdminScenarioDto(it.id!!, it.title ?: "Unnamed Scenario")
                }

        return AdminDashboardDto(
                schools = schoolDtos,
                courses = courses,
                tests = tests,
                scenarios = scenarios
        )
    }

    @PostMapping("/courses/add")
    fun createCourse(@RequestBody req: CreateCourseRequest): AdminCourseDto {
        val course =
                Course(title = req.title, description = req.description, ageGroup = req.ageGroup)
        val saved = courseRepo.save(course)
        return AdminCourseDto(saved.id!!, saved.title, saved.description, saved.ageGroup, emptyList())
    }

    @PostMapping("/courses/update/{id}")
    fun updateCourse(
            @org.springframework.web.bind.annotation.PathVariable id: Long,
            @RequestBody req: UpdateCourseRequest
    ): AdminCourseDto {
        val course = courseRepo.findById(id).orElseThrow { error("Course not found") }
        if (req.title != null) course.title = req.title
        if (req.description != null) course.description = req.description
        if (req.ageGroup != null) course.ageGroup = req.ageGroup
        val saved = courseRepo.save(course)
        val lessons = lessonRepo.findAllByCourseIdOrderByOrderNumberAsc(saved.id!!)
            .map { AdminLessonDto(it.id!!, it.title, it.orderNumber, it.videoPath, it.textContent) }
        return AdminCourseDto(saved.id!!, saved.title, saved.description, saved.ageGroup, lessons)
    }

    @PostMapping("/courses/delete/{id}")
    fun deleteCourse(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val course = courseRepo.findById(id).orElseThrow { error("Course not found") }
        courseRepo.delete(course)
    }

    @PostMapping("/lessons/add")
    fun createLesson(@RequestBody req: CreateLessonRequest): AdminLessonDto {
        val course = courseRepo.findById(req.courseId).orElseThrow { error("Course not found") }
        val lesson = Lesson(
            course = course,
            title = req.title,
            videoPath = req.videoPath,
            textContent = req.textContent,
            orderNumber = req.orderNumber
        )
        val saved = lessonRepo.save(lesson)
        return AdminLessonDto(saved.id!!, saved.title, saved.orderNumber, saved.videoPath, saved.textContent)
    }

    @PostMapping("/lessons/update/{id}")
    fun updateLesson(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateLessonRequest
    ): AdminLessonDto {
        val lesson = lessonRepo.findById(id).orElseThrow { error("Lesson not found") }
        if (req.title != null) lesson.title = req.title
        if (req.videoPath != null) lesson.videoPath = req.videoPath
        if (req.textContent != null) lesson.textContent = req.textContent
        if (req.orderNumber != null) lesson.orderNumber = req.orderNumber
        val saved = lessonRepo.save(lesson)
        return AdminLessonDto(saved.id!!, saved.title, saved.orderNumber, saved.videoPath, saved.textContent)
    }

    @PostMapping("/lessons/delete/{id}")
    fun deleteLesson(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val lesson = lessonRepo.findById(id).orElseThrow { error("Lesson not found") }
        lessonRepo.delete(lesson)
    }
}
