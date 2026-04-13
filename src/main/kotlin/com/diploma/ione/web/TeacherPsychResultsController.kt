package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.ZoneType
import com.diploma.ione.repo.*
import com.diploma.ione.service.TestScoringService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TeacherTestListDto(
    val id: Long,
    val title: String,
    val description: String?
)

data class TeacherLatestAttemptBriefDto(
    val attemptId: Long,
    val testId: Long,
    val testTitle: String,
    val finishedAt: String?,
    val maxZone: String
)

data class TeacherStudentLatestAttemptDto(
    val studentId: Long,
    val studentName: String,
    val className: String?,
    val latestAttempt: TeacherLatestAttemptBriefDto?
)

data class TeacherStudentTestAttemptSummaryDto(
    val attemptId: Long,
    val testId: Long,
    val testTitle: String,
    val startedAt: String,
    val finishedAt: String?,
    val maxZone: String,
    val categoryResults: List<CategoryResultDto>
)

@RestController
@RequestMapping("/api/teacher")
class TeacherPsychResultsController(
    private val teacherRepo: TeacherRepo,
    private val studentRepo: StudentRepo,
    private val testRepo: PsychologicalTestRepo,
    private val attemptRepo: TestAttemptRepo,
    private val attemptAnswerRepo: TestAttemptAnswerRepo,
    private val categoryRepo: TestCategoryRepo,
    private val scoring: TestScoringService
) {
    @GetMapping("/tests")
    fun listTests(): List<TeacherTestListDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }
        return testRepo.findAll().map { TeacherTestListDto(it.id!!, it.title, it.description) }
    }

    /**
     * Students that belong to the current teacher + their latest finished attempt (across any test).
     * Used for the teacher "auto results" dashboard list.
     */
    @GetMapping("/tests/students/latest")
    fun studentsWithLatestFinishedAttempt(): List<TeacherStudentLatestAttemptDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val students = studentRepo.findAll().filter { it.teacher.id == teacherId }

        return students.map { s ->
            val attempts = attemptRepo.findAllByStudentIdWithTestOrderByStartedAtDesc(s.id!!)
            val latestFinished = attempts.firstOrNull { it.isFinished }

            val latestBrief = latestFinished?.let { a ->
                val raw = attemptAnswerRepo.sumScoresByCategory(a.id!!)
                val scoresByCategory = raw.associate {
                    (it[0] as Number).toLong() to (it[1] as Number).toInt()
                }
                val (_, maxZone) = scoring.resolveZones(scoresByCategory)

                TeacherLatestAttemptBriefDto(
                    attemptId = a.id!!,
                    testId = a.test.id!!,
                    testTitle = a.test.title,
                    finishedAt = a.finishedAt?.toString(),
                    maxZone = maxZone.name
                )
            }

            TeacherStudentLatestAttemptDto(
                studentId = s.id!!,
                studentName = s.user.fullName,
                className = s.className,
                latestAttempt = latestBrief
            )
        }.sortedWith(
            compareByDescending<TeacherStudentLatestAttemptDto> { dto ->
                dto.latestAttempt?.maxZone?.let { severity(ZoneType.valueOf(it)) } ?: -1
            }.thenBy { it.studentName }
        )
    }

    /**
     * Latest finished attempt per test for a single student (must belong to the current teacher).
     * Returns category-level results (zone per category), without full answer table to keep the payload small.
     */
    @GetMapping("/tests/students/{studentId}/results")
    fun studentLatestResults(@PathVariable studentId: Long): List<TeacherStudentTestAttemptSummaryDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val student = studentRepo.findById(studentId).orElseThrow { error("Student not found") }
        if (student.teacher.id != teacherId) error("Forbidden: not your student")

        val attempts = attemptRepo.findAllByStudentIdWithTestOrderByStartedAtDesc(studentId)
            .filter { it.isFinished }

        val latestByTest = attempts
            .groupBy { it.test.id!! }
            .mapNotNull { (_, list) -> list.firstOrNull() }
            .sortedBy { it.test.title }

        return latestByTest.map { attempt ->
            val attemptId = attempt.id!!
            val raw = attemptAnswerRepo.sumScoresByCategory(attemptId)
            val scoresByCategory = raw.associate {
                (it[0] as Number).toLong() to (it[1] as Number).toInt()
            }

            val (zoneByCategory, maxZone) = scoring.resolveZones(scoresByCategory)

            val categories = categoryRepo.findAllByTestId(attempt.test.id!!)
                .associateBy { it.id!! }

            val categoryResults = scoresByCategory.entries.map { (catId, total) ->
                val cat = categories[catId]
                CategoryResultDto(
                    categoryId = catId,
                    categoryName = cat?.name ?: "Category $catId",
                    totalScore = total,
                    zone = (zoneByCategory[catId] ?: ZoneType.GREEN).name
                )
            }.sortedBy { it.categoryName }

            TeacherStudentTestAttemptSummaryDto(
                attemptId = attemptId,
                testId = attempt.test.id!!,
                testTitle = attempt.test.title,
                startedAt = attempt.startedAt.toString(),
                finishedAt = attempt.finishedAt?.toString(),
                maxZone = maxZone.name,
                categoryResults = categoryResults
            )
        }
    }

    private fun severity(z: ZoneType): Int = when (z) {
        ZoneType.GREEN -> 0
        ZoneType.YELLOW -> 1
        ZoneType.RED -> 2
        ZoneType.BLACK -> 3
    }
}

