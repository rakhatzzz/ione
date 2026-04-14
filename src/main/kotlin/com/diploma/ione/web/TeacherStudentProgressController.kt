package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.LessonProgressStatus
import com.diploma.ione.repo.CourseRepo
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.ScenarioRepo
import com.diploma.ione.repo.StudentLessonProgressRepo
import com.diploma.ione.repo.StudentRepo
import com.diploma.ione.repo.StudentScenarioAnswerRepo
import com.diploma.ione.repo.TeacherRepo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TeacherScenarioProgressDto(
    val scenarioId: Long,
    val title: String,
    val completed: Boolean,
    val selectedOptionText: String?,
    val selectedOptionScore: Int?,
    val resultText: String?
)

data class TeacherLessonScenarioProgressDto(
    val lessonId: Long,
    val lessonTitle: String,
    val totalScenarios: Int,
    val completedScenarios: Int,
    val scenarios: List<TeacherScenarioProgressDto>
)

data class TeacherCourseProgressDto(
    val courseId: Long,
    val courseTitle: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val completed: Boolean,
    val totalScenarios: Int,
    val completedScenarios: Int,
    val lessonScenarioProgress: List<TeacherLessonScenarioProgressDto>
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
    private val progressRepo: StudentLessonProgressRepo,
    private val scenarioRepo: ScenarioRepo,
    private val scenarioAnswerRepo: StudentScenarioAnswerRepo
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
                val lessonIds = lessons.mapNotNull { it.id }
                val scenarios = if (lessonIds.isEmpty()) emptyList() else scenarioRepo.findAllByLessonIdIn(lessonIds)
                val scenariosByLessonId = scenarios.groupBy { it.lesson.id!! }
                val answersByScenarioId =
                    scenarioAnswerRepo.findAllByStudentIdAndScenarioLessonCourseId(student.id!!, course.id!!)
                        .associateBy { it.scenario.id!! }

                val completedLessons = progressRepo.findAllByStudentIdAndLessonCourseId(student.id!!, course.id!!)
                    .count { it.status == LessonProgressStatus.COMPLETED }

                val lessonScenarioProgress =
                    lessons.map { lesson ->
                        val lessonScenarios = (scenariosByLessonId[lesson.id!!] ?: emptyList()).sortedBy { it.id ?: 0L }
                        val scenarioProgress =
                            lessonScenarios.map { scenario ->
                                val answer = answersByScenarioId[scenario.id!!]
                                TeacherScenarioProgressDto(
                                    scenarioId = scenario.id!!,
                                    title = scenario.title ?: "Сценарий #${scenario.id}",
                                    completed = answer != null,
                                    selectedOptionText = answer?.selectedOption?.optionText,
                                    selectedOptionScore = answer?.selectedOption?.score,
                                    resultText = answer?.selectedOption?.resultText
                                )
                            }
                        TeacherLessonScenarioProgressDto(
                            lessonId = lesson.id!!,
                            lessonTitle = lesson.title,
                            totalScenarios = scenarioProgress.size,
                            completedScenarios = scenarioProgress.count { it.completed },
                            scenarios = scenarioProgress
                        )
                    }

                val totalScenarios = lessonScenarioProgress.sumOf { it.totalScenarios }
                val completedScenarios = lessonScenarioProgress.sumOf { it.completedScenarios }

                TeacherCourseProgressDto(
                    courseId = course.id!!,
                    courseTitle = course.title,
                    totalLessons = lessons.size,
                    completedLessons = completedLessons,
                    completed = lessons.isNotEmpty() && completedLessons == lessons.size,
                    totalScenarios = totalScenarios,
                    completedScenarios = completedScenarios,
                    lessonScenarioProgress = lessonScenarioProgress
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

