package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.TestAttempt
import com.diploma.ione.domain.TestAttemptAnswer
import com.diploma.ione.repo.*
import com.diploma.ione.service.TestScoringService
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/student/tests")
class StudentTestController(
    private val studentRepo: StudentRepo,
    private val testRepo: PsychologicalTestRepo,
    private val questionRepo: TestQuestionRepo,
    private val optionRepo: TestAnswerOptionRepo,
    private val attemptRepo: TestAttemptRepo,
    private val attemptAnswerRepo: TestAttemptAnswerRepo,
    private val categoryRepo: TestCategoryRepo,
    private val scoring: TestScoringService
) {
    @GetMapping
    fun listTests(): List<TestListDto> =
        testRepo.findAll().map { TestListDto(it.id!!, it.title, it.description) }

    @GetMapping("/{testId}")
    fun getTest(@PathVariable testId: Long): List<TestQuestionDto> {
        val questions = questionRepo.findAllByTestIdOrderByOrderNumberAsc(testId)
        return questions.map { q ->
            val opts = optionRepo.findAllByQuestionId(q.id!!)
            TestQuestionDto(
                id = q.id!!,
                categoryId = q.category.id!!,
                categoryName = q.category.name,
                text = q.text,
                orderNumber = q.orderNumber,
                options = opts.map { TestOptionDto(it.id!!, it.text) }
            )
        }
    }

    @PostMapping("/{testId}/attempts/start")
    fun start(@PathVariable testId: Long): StartAttemptResponse {
        val studentId = AuthUtil.currentUserId()
        val student = studentRepo.findById(studentId).orElseThrow { error("Student not found") }
        val test = testRepo.findById(testId).orElseThrow { error("Test not found") }

        val attempt = attemptRepo.save(TestAttempt(student = student, test = test))
        return StartAttemptResponse(attempt.id!!)
    }

    @PostMapping("/attempts/{attemptId}/questions/{questionId}/answer")
    fun answer(
        @PathVariable attemptId: Long,
        @PathVariable questionId: Long,
        @RequestBody req: AnswerQuestionRequest
    ): Map<String, Any?> {
        val studentId = AuthUtil.currentUserId()
        val attempt = attemptRepo.findById(attemptId).orElseThrow { error("Attempt not found") }

        if (attempt.student.id != studentId) error("Not your attempt")
        if (attempt.isFinished) error("Attempt is finished")

        val question = questionRepo.findById(questionId).orElseThrow { error("Question not found") }
        if (question.test.id != attempt.test.id) error("Question does not belong to this test")

        val option = optionRepo.findById(req.optionId).orElseThrow { error("Option not found") }
        if (option.question.id != question.id) error("Option does not belong to question")

        val now = LocalDateTime.now()
        val existing = attemptAnswerRepo.findByAttemptIdAndQuestionId(attemptId, questionId)

        val saved = if (existing != null) {
            existing.selectedOption = option
            existing.score = option.score
            existing.createdAt = now
            attemptAnswerRepo.save(existing)
        } else {
            attemptAnswerRepo.save(
                TestAttemptAnswer(
                    attempt = attempt,
                    question = question,
                    selectedOption = option,
                    score = option.score,
                    createdAt = now
                )
            )
        }

        return mapOf("answerId" to saved.id, "score" to saved.score)
    }

    @PostMapping("/attempts/{attemptId}/finish")
    fun finish(@PathVariable attemptId: Long): FinishAttemptResponse {
        val studentId = AuthUtil.currentUserId()
        val attempt = attemptRepo.findById(attemptId).orElseThrow { error("Attempt not found") }

        if (attempt.student.id != studentId) error("Not your attempt")
        if (attempt.isFinished) {
        } else {
            attempt.isFinished = true
            attempt.finishedAt = LocalDateTime.now()
            attemptRepo.save(attempt)
        }

        val raw = attemptAnswerRepo.sumScoresByCategory(attemptId)
        val scoresByCategory = raw.associate {
            val categoryId = (it[0] as Number).toLong()
            val totalScore = (it[1] as Number).toInt()
            categoryId to totalScore
        }

        val (zoneByCategory, maxZone) = scoring.resolveZones(scoresByCategory)

        val categories = categoryRepo.findAllByTestId(attempt.test.id!!)
            .associateBy { it.id!! }

        val results = scoresByCategory.entries.map { (catId, total) ->
            val cat = categories[catId]
            CategoryResultDto(
                categoryId = catId,
                categoryName = cat?.name ?: "Category $catId",
                totalScore = total,
                zone = (zoneByCategory[catId] ?: com.diploma.ione.domain.ZoneType.GREEN).name
            )
        }.sortedBy { it.categoryName }

        return FinishAttemptResponse(
            attemptId = attemptId,
            results = results,
            maxZone = maxZone.name
        )
    }
}