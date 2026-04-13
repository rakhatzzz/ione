package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.LessonProgressStatus
import com.diploma.ione.repo.CourseRepo
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.StudentLessonProgressRepo
import com.diploma.ione.repo.StudentRepo
import com.diploma.ione.repo.TeacherRepo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TeacherCourseProgressDto(
    val courseId: Long,
    val courseTitle: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val completed: Boolean
)

data class TeacherStudentCourseProgressDto(
    val studentId: Long,
    val studentName: String,
    val className: String?,
    val courses: List<TeacherCourseProgressDto>
)

@RestController
@RequestMapping("/api/teacher")
class TeacherStudentProgressController(
    private val teacherRepo: TeacherRepo,
    private val studentRepo: StudentRepo,
    private val courseRepo: CourseRepo,
    private val lessonRepo: LessonRepo,
    private val progressRepo: StudentLessonProgressRepo
) {
    @GetMapping("/students/course-progress")
    fun courseProgressByStudent(): List<TeacherStudentCourseProgressDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val students = studentRepo.findAll().filter { it.teacher.id == teacherId }
        val courses = courseRepo.findAll()

        return students.map { student ->
            val courseDtos = courses.map { course ->
                val lessons = lessonRepo.findAllByCourseIdOrderByOrderNumberAsc(course.id!!)
                val completedLessons = progressRepo.findAllByStudentIdAndLessonCourseId(student.id!!, course.id!!)
                    .count { it.status == LessonProgressStatus.COMPLETED }

                TeacherCourseProgressDto(
                    courseId = course.id!!,
                    courseTitle = course.title,
                    totalLessons = lessons.size,
                    completedLessons = completedLessons,
                    completed = lessons.isNotEmpty() && completedLessons == lessons.size
                )
            }.sortedBy { it.courseTitle }

            TeacherStudentCourseProgressDto(
                studentId = student.id!!,
                studentName = student.user.fullName,
                className = student.className,
                courses = courseDtos
            )
        }.sortedBy { it.studentName }
    }
}

