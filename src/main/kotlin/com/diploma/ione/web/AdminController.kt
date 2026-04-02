package com.diploma.ione.web

import com.diploma.ione.domain.Course
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

data class AdminCourseDto(val id: Long, val title: String)

data class AdminTestDto(val id: Long, val title: String)

data class AdminScenarioDto(val id: Long, val title: String)

data class CreateCourseRequest(val title: String, val description: String?, val ageGroup: String?)

data class UpdateCourseRequest(val title: String?, val description: String?, val ageGroup: String?)

@RestController
@RequestMapping("/api/admin")
class AdminController(
        private val schoolRepo: SchoolRepo,
        private val teacherRepo: TeacherRepo,
        private val studentRepo: StudentRepo,
        private val courseRepo: CourseRepo,
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

        val courses = courseRepo.findAll().map { AdminCourseDto(it.id!!, it.title) }
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
        return AdminCourseDto(saved.id!!, saved.title)
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
        return AdminCourseDto(saved.id!!, saved.title)
    }

    @PostMapping("/courses/delete/{id}")
    fun deleteCourse(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val course = courseRepo.findById(id).orElseThrow { error("Course not found") }
        courseRepo.delete(course)
    }
}
