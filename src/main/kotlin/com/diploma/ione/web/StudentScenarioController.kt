package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.StudentScenarioAnswer
import com.diploma.ione.repo.*
import jakarta.validation.constraints.NotNull
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

data class ScenarioAnswerRequest(@field:NotNull val optionId: Long)

@RestController
@RequestMapping("/api/student")
class StudentScenarioController(
    private val studentRepo: StudentRepo,
    private val scenarioRepo: ScenarioRepo,
    private val optionRepo: ScenarioOptionRepo,
    private val answerRepo: StudentScenarioAnswerRepo
) {
    @GetMapping("/lessons/{lessonId}/scenario")
    fun getLessonScenario(@PathVariable lessonId: Long): StudentLessonScenarioDto {
        val scenario = scenarioRepo.findByLessonId(lessonId).orElse(null)
            ?: return StudentLessonScenarioDto(available = false)

        val options = optionRepo.findAllByScenarioId(scenario.id!!)
            .map { StudentScenarioOptionDto(it.id!!, it.optionText) }

        return StudentLessonScenarioDto(
            available = true,
            scenarioId = scenario.id,
            title = scenario.title,
            description = scenario.description,
            options = options
        )
    }

    @PostMapping("/scenarios/{scenarioId}/answer")
    fun answer(@PathVariable scenarioId: Long, @RequestBody req: ScenarioAnswerRequest): Map<String, Any?> {
        val studentId = AuthUtil.currentUserId()
        val student = studentRepo.findById(studentId).orElseThrow { error("Student not found") }
        val scenario = scenarioRepo.findById(scenarioId).orElseThrow { error("Scenario not found") }
        val option = optionRepo.findById(req.optionId).orElseThrow { error("Option not found") }

        if (option.scenario.id != scenario.id) error("Option does not belong to scenario")

        val existing = answerRepo.findByStudentIdAndScenarioId(studentId, scenarioId)
        val saved = if (existing != null) {
            existing.selectedOption = option
            existing.createdAt = LocalDateTime.now()
            answerRepo.save(existing)
        } else {
            answerRepo.save(StudentScenarioAnswer(student = student, scenario = scenario, selectedOption = option))
        }

        return mapOf(
            "answerId" to saved.id,
            "selectedOptionId" to option.id,
            "resultText" to option.resultText,
            "resultImageUrl" to option.resultImagePath?.let { "/media/$it" },
            "score" to option.score
        )
    }
}
