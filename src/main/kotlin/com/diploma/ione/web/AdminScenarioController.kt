package com.diploma.ione.web

import com.diploma.ione.domain.Scenario
import com.diploma.ione.domain.ScenarioOption
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.ScenarioOptionRepo
import com.diploma.ione.repo.ScenarioRepo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateScenarioRequest(val lessonId: Long, val title: String, val description: String?, val baseImagePath: String?)
data class UpdateScenarioRequest(val title: String?, val description: String?, val baseImagePath: String?)

data class CreateScenarioOptionRequest(val scenarioId: Long, val optionText: String, val resultText: String, val resultImagePath: String?, val score: Int)
data class UpdateScenarioOptionRequest(val optionText: String?, val resultText: String?, val resultImagePath: String?, val score: Int?)

@RestController
@RequestMapping("/api/admin")
class AdminScenarioController(
    private val lessonRepo: LessonRepo,
    private val scenarioRepo: ScenarioRepo,
    private val optionRepo: ScenarioOptionRepo
) {
    @PostMapping("/scenarios/add")
    fun addScenario(@RequestBody req: CreateScenarioRequest): ResponseEntity<Any> {
        val lesson = lessonRepo.findById(req.lessonId).orElseThrow { error("Lesson not found") }
        val scenario = Scenario(lesson = lesson, title = req.title, description = req.description, baseImagePath = req.baseImagePath)
        val saved = scenarioRepo.save(scenario)
        return ResponseEntity.ok(mapOf("id" to saved.id, "title" to saved.title))
    }

    @PostMapping("/scenarios/update/{id}")
    fun updateScenario(@PathVariable id: Long, @RequestBody req: UpdateScenarioRequest): ResponseEntity<Any> {
        val scenario = scenarioRepo.findById(id).orElseThrow { error("Scenario not found") }
        req.title?.let { scenario.title = it }
        req.description?.let { scenario.description = it }
        req.baseImagePath?.let { scenario.baseImagePath = it }
        val saved = scenarioRepo.save(scenario)
        return ResponseEntity.ok(mapOf("id" to saved.id, "title" to saved.title))
    }

    @PostMapping("/scenarios/delete/{id}")
    fun deleteScenario(@PathVariable id: Long): ResponseEntity<Any> {
        scenarioRepo.deleteById(id)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    @PostMapping("/scenario-options/add")
    fun addScenarioOption(@RequestBody req: CreateScenarioOptionRequest): ResponseEntity<Any> {
        val scenario = scenarioRepo.findById(req.scenarioId).orElseThrow { error("Scenario not found") }
        val option = ScenarioOption(
            scenario = scenario,
            optionText = req.optionText,
            resultText = req.resultText,
            resultImagePath = req.resultImagePath,
            score = req.score
        )
        val saved = optionRepo.save(option)
        return ResponseEntity.ok(mapOf("id" to saved.id))
    }

    @PostMapping("/scenario-options/update/{id}")
    fun updateScenarioOption(@PathVariable id: Long, @RequestBody req: UpdateScenarioOptionRequest): ResponseEntity<Any> {
        val option = optionRepo.findById(id).orElseThrow { error("Option not found") }
        req.optionText?.let { option.optionText = it }
        req.resultText?.let { option.resultText = it }
        req.resultImagePath?.let { option.resultImagePath = it }
        req.score?.let { option.score = it }
        val saved = optionRepo.save(option)
        return ResponseEntity.ok(mapOf("id" to saved.id))
    }

    @PostMapping("/scenario-options/delete/{id}")
    fun deleteScenarioOption(@PathVariable id: Long): ResponseEntity<Any> {
        optionRepo.deleteById(id)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}