package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.ZoneType
import com.diploma.ione.repo.*
import com.diploma.ione.service.TestScoringService
import org.springframework.web.bind.annotation.*

data class RiskStudentDto(
    val studentId: Long,
    val studentName: String,
    val className: String?,
    val attemptId: Long,
    val maxZone: String
)

@RestController
@RequestMapping("/api/teacher")
class TeacherRiskController(
    private val teacherRepo: TeacherRepo,
    private val studentRepo: StudentRepo,
    private val testRepo: PsychologicalTestRepo,
    private val attemptRepo: TestAttemptRepo,
    private val attemptAnswerRepo: TestAttemptAnswerRepo,
    private val scoring: TestScoringService
) {

    @GetMapping("/risk-students")
    fun riskStudents(
        @RequestParam testId: Long,
        @RequestParam(defaultValue = "YELLOW") minZone: String
    ): List<RiskStudentDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }
        val test = testRepo.findById(testId).orElseThrow { error("Test not found") }

        val min = ZoneType.valueOf(minZone)

        val students = studentRepo.findAll()
            .filter { it.teacher.id == teacherId }

        val result = mutableListOf<RiskStudentDto>()

        for (s in students) {
            val attempts = attemptRepo.findAllByStudentIdAndTestIdOrderByStartedAtDesc(s.id!!, test.id!!)
            val lastAttempt = attempts.firstOrNull { it.isFinished } ?: continue
            val raw = attemptAnswerRepo.sumScoresByCategory(lastAttempt.id!!)
            val scoresByCategory = raw.associate {
                (it[0] as Number).toLong() to (it[1] as Number).toInt()
            }
            val (_, maxZone) = scoring.resolveZones(scoresByCategory)

            if (severity(maxZone) >= severity(min)) {
                result.add(
                    RiskStudentDto(
                        studentId = s.id!!,
                        studentName = s.user.fullName,
                        className = s.className,
                        attemptId = lastAttempt.id!!,
                        maxZone = maxZone.name
                    )
                )
            }
        }

        return result.sortedByDescending { severity(ZoneType.valueOf(it.maxZone)) }
    }

    private fun severity(z: ZoneType): Int = when (z) {
        ZoneType.GREEN -> 0
        ZoneType.YELLOW -> 1
        ZoneType.RED -> 2
        ZoneType.BLACK -> 3
    }
}