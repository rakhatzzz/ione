package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

enum class LessonTicketStatus {
    PENDING, APPROVED, REJECTED, IMPLEMENTED
}

@Entity
@Table(name = "lesson_tickets")
class LessonTicket(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    var teacher: Teacher,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LessonTicketStatus = LessonTicketStatus.PENDING,

    @Column(name = "admin_note", columnDefinition = "text")
    var adminNote: String? = null,

    @Column(name = "created_lesson_id")
    var createdLessonId: Long? = null,

    @Column(name = "suggested_course_id")
    var suggestedCourseId: Long? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
