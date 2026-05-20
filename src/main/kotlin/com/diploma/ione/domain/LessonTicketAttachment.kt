package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

enum class LessonTicketAttachmentKind {
    DOCUMENT, VIDEO, IMAGE, OTHER
}

@Entity
@Table(name = "lesson_ticket_attachments")
class LessonTicketAttachment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: LessonTicket,

    @Column(name = "file_url", nullable = false)
    var fileUrl: String,

    @Column(name = "original_name")
    var originalName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kind: LessonTicketAttachmentKind = LessonTicketAttachmentKind.OTHER,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
