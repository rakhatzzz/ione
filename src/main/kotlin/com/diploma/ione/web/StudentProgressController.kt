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
        val userId = AuthUtil.currentUserId()
        val student = studentRepo.findById(userId)
            .orElseThrow { RuntimeException("Student with ID $userId not found in database. Please register again.") }

        val studentId = student.id ?: error("Student ID not found")
        val lesson = lessonRepo.findById(lessonId)
            .orElseThrow { RuntimeException("Lesson not found") }

        val now = LocalDateTime.now()
        
        // Get or create progress record using studentId
        val progress = progressRepo.findByStudentIdAndLessonId(studentId, lessonId)
            ?.apply {
                status = LessonProgressStatus.COMPLETED
                completedAt = now
                updatedAt = now
                if (startedAt == null) startedAt = now
            }
            ?: StudentLessonProgress(
                student = student,
                lesson = lesson,
                status = LessonProgressStatus.COMPLETED,
                startedAt = now,
                completedAt = now,
                updatedAt = now
            )

        progressRepo.save(progress)
        progressRepo.flush()

        return mapOf(
            "lessonId" to lessonId,
            "status" to progress.status.name,
            "completedAt" to progress.completedAt.toString()
        )
    }
}
