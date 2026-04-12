package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.LessonProgressStatus
import com.diploma.ione.domain.StudentScenarioAnswer
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.ScenarioOptionRepo
import com.diploma.ione.repo.ScenarioRepo
import com.diploma.ione.repo.StudentLessonProgressRepo
import com.diploma.ione.repo.StudentRepo
import com.diploma.ione.repo.StudentScenarioAnswerRepo
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class ScenarioAnswerRequest(@field:NotNull val optionId: Long)

@RestController
@RequestMapping("/api/student")
class StudentScenarioController(
    private val studentRepo: StudentRepo,
    private val lessonRepo: LessonRepo,
    private val scenarioRepo: ScenarioRepo,
    private val optionRepo: ScenarioOptionRepo,
    private val answerRepo: StudentScenarioAnswerRepo,
    private val progressRepo: StudentLessonProgressRepo
) {
    @GetMapping("/courses/{courseId}/lessons/{lessonId}/scenario")
    fun getLessonScenario(
        @PathVariable courseId: Long,
        @PathVariable lessonId: Long
    ): StudentLessonScenarioDto {
        val studentId = AuthUtil.currentUserId()
        val lesson =
            lessonRepo.findById(lessonId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не найден")
            }
        if (lesson.course.id != courseId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Урок не относится к этому курсу")
        }

        val scenario = scenarioRepo.findByLessonId(lessonId).orElse(null)
            ?: return StudentLessonScenarioDto(available = false, completed = false, hasScenario = false)

        val lessonCompleted =
            progressRepo.findByStudentIdAndLessonId(studentId, lessonId)?.status ==
                LessonProgressStatus.COMPLETED

        val existing = answerRepo.findByStudentIdAndScenarioId(studentId, scenario.id!!)
        if (existing != null) {
            val opt = existing.selectedOption
            return StudentLessonScenarioDto(
                available = false,
                completed = true,
                hasScenario = true,
                scenarioId = scenario.id,
                title = scenario.title,
                description = scenario.description,
                baseImageUrl = ScenarioMediaUrls.resolve(scenario.baseImagePath),
                options = emptyList(),
                message = null,
                selectedOptionText = opt.optionText,
                resultText = opt.resultText,
                resultImageUrl = ScenarioMediaUrls.resolve(opt.resultImagePath)
            )
        }

        if (!lessonCompleted) {
            return StudentLessonScenarioDto(
                available = false,
                completed = false,
                hasScenario = true,
                scenarioId = scenario.id,
                title = scenario.title,
                description = scenario.description,
                baseImageUrl = null,
                options = emptyList(),
                message = "Сначала завершите урок, чтобы открыть ситуационный тест."
            )
        }

        val options =
            optionRepo.findAllByScenarioId(scenario.id!!).map {
                StudentScenarioOptionDto(it.id!!, it.optionText)
            }

        return StudentLessonScenarioDto(
            available = true,
            completed = false,
            hasScenario = true,
            scenarioId = scenario.id,
            title = scenario.title,
            description = scenario.description,
            baseImageUrl = ScenarioMediaUrls.resolve(scenario.baseImagePath),
            options = options,
            message = null
        )
    }

    @PostMapping("/scenarios/{scenarioId}/answer")
    fun answer(@PathVariable scenarioId: Long, @RequestBody req: ScenarioAnswerRequest): Map<String, Any?> {
        val studentId = AuthUtil.currentUserId()
        val student = studentRepo.findById(studentId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val scenario = scenarioRepo.findById(scenarioId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val option = optionRepo.findById(req.optionId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        if (option.scenario.id != scenario.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Вариант не относится к этому сценарию")
        }

        val lessonId = scenario.lesson.id!!
        val progress = progressRepo.findByStudentIdAndLessonId(studentId, lessonId)
        if (progress == null || progress.status != LessonProgressStatus.COMPLETED) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Сначала завершите урок")
        }

        val existing = answerRepo.findByStudentIdAndScenarioId(studentId, scenarioId)
        if (existing != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ситуационный тест уже пройден")
        }

        val saved =
            answerRepo.save(
                StudentScenarioAnswer(student = student, scenario = scenario, selectedOption = option)
            )

        return mapOf(
            "answerId" to saved.id,
            "selectedOptionId" to option.id,
            "selectedOptionText" to option.optionText,
            "resultText" to option.resultText,
            "resultImageUrl" to ScenarioMediaUrls.resolve(option.resultImagePath)
        )
    }
}
