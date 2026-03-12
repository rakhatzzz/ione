package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.LessonProgressStatus
import com.diploma.ione.domain.StudentLessonProgress
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.StudentLessonProgressRepo
import com.diploma.ione.repo.StudentRepo
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/student")
class StudentProgressController(
    private val studentRepo: StudentRepo,
    private val lessonRepo: LessonRepo,
    private val progressRepo: StudentLessonProgressRepo
) {
    @PostMapping("/lessons/{lessonId}/complete")
    fun completeLesson(@PathVariable lessonId: Long): Map<String, Any> {
        val studentId = AuthUtil.currentUserId()
        val student = studentRepo.findById(studentId).orElseThrow { error("Student not found") }
        val lesson = lessonRepo.findById(lessonId).orElseThrow { error("Lesson not found") }

        val now = LocalDateTime.now()
        val progress = progressRepo.findByStudentIdAndLessonId(studentId, lessonId)
            ?: StudentLessonProgress(student = student, lesson = lesson)

        if (progress.startedAt == null) progress.startedAt = now
        progress.status = LessonProgressStatus.COMPLETED
        progress.completedAt = now
        progress.updatedAt = now

        progressRepo.save(progress)

        return mapOf("lessonId" to lessonId, "status" to progress.status.name, "completedAt" to progress.completedAt.toString())
    }
}