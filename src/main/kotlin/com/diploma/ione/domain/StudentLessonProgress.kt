package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "student_lesson_progress",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "lesson_id"])]
)
class StudentLessonProgress(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    var lesson: Lesson,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LessonProgressStatus = LessonProgressStatus.NOT_STARTED,

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

)

enum class LessonProgressStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED
}