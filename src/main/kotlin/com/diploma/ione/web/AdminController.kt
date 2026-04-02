package com.diploma.ione.web

import com.diploma.ione.repo.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminDashboardDto(
    val schools: List<AdminSchoolDto>,
    val courses: List<AdminCourseDto>,
    val tests: List<AdminTestDto>,
    val scenarios: List<AdminScenarioDto>
)

data class AdminSchoolDto(
    val id: Long,
    val name: String,
    val teachers: List<AdminTeacherDto>
)

data class AdminTeacherDto(
    val id: Long,
    val fullName: String,
    val students: List<AdminStudentDto>
)

data class AdminStudentDto(
    val id: Long,
    val fullName: String,
    val className: String?
)

data class AdminCourseDto(
    val id: Long,
    val title: String
)

data class AdminTestDto(
    val id: Long,
    val title: String
)

data class AdminScenarioDto(
    val id: Long,
    val title: String
)

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

        val schoolDtos = schools.map { school ->
            val schoolTeachers = teachersBySchool[school.id] ?: emptyList()
            val teacherDtos = schoolTeachers.map { teacher ->
                val teacherStudents = studentsByTeacher[teacher.id] ?: emptyList()
                val studentDtos = teacherStudents.map { student ->
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
            AdminSchoolDto(
                id = school.id!!,
                name = school.name,
                teachers = teacherDtos
            )
        }

        val courses = courseRepo.findAll().map { AdminCourseDto(it.id!!, it.title) }
        val tests = testRepo.findAll().map { AdminTestDto(it.id!!, it.title) }
        val scenarios = scenarioRepo.findAll().map { AdminScenarioDto(it.id!!, it.title ?: "Unnamed Scenario") }

        return AdminDashboardDto(
            schools = schoolDtos,
            courses = courses,
            tests = tests,
            scenarios = scenarios
        )
    }
}
