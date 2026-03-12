package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.ZoneType
import com.diploma.ione.repo.*
import com.diploma.ione.service.TestScoringService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/teacher")
class TeacherAttemptController(
    private val teacherRepo: TeacherRepo,
    private val attemptRepo: TestAttemptRepo,
    private val attemptAnswerRepo: TestAttemptAnswerRepo,
    private val categoryRepo: TestCategoryRepo,
    private val scoring: TestScoringService
) {

    @GetMapping("/test-attempts/{attemptId}")
    fun attemptDetails(@PathVariable attemptId: Long): TeacherAttemptDetailsDto {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val attempt = attemptRepo.findByIdWithStudentAndTest(attemptId)
            ?: error("Attempt not found")

        if (attempt.student.teacher.id != teacherId) {
            error("Forbidden: not your student")
        }

        val answers = attemptAnswerRepo.findAllByAttemptIdDetailed(attemptId)

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

        val answerDtos = answers.map { a ->
            AttemptAnswerDto(
                questionId = a.question.id!!,
                orderNumber = a.question.orderNumber,
                categoryId = a.question.category.id!!,
                categoryName = a.question.category.name,
                questionText = a.question.text,
                selectedOptionId = a.selectedOption.id!!,
                selectedOptionText = a.selectedOption.text,
                score = a.score
            )
        }

        return TeacherAttemptDetailsDto(
            attemptId = attempt.id!!,
            testId = attempt.test.id!!,
            testTitle = attempt.test.title,
            studentId = attempt.student.id!!,
            studentName = attempt.student.user.fullName,
            className = attempt.student.className,
            isFinished = attempt.isFinished,
            startedAt = attempt.startedAt.toString(),
            finishedAt = attempt.finishedAt?.toString(),
            maxZone = maxZone.name,
            categoryResults = categoryResults,
            answers = answerDtos
        )
    }
}