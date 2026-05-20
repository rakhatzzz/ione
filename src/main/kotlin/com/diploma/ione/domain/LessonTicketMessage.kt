package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

enum class LessonTicketMessageSender {
    TEACHER, ADMIN
}

@Entity
@Table(name = "lesson_ticket_messages")
class LessonTicketMessage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: LessonTicket,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var sender: LessonTicketMessageSender,

    @Column(name = "sender_user_id", nullable = false)
    var senderUserId: Long,

    @Column(columnDefinition = "text", nullable = false)
    var text: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
