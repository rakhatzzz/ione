package com.diploma.ione.web

data class CreateLessonTicketRequest(
    val title: String,
    val description: String?
)

data class LessonTicketAttachmentDto(
    val id: Long,
    val fileUrl: String,
    val originalName: String?,
    val kind: String,
    val createdAt: String
)

data class LessonTicketMessageDto(
    val id: Long,
    val sender: String,
    val senderUserId: Long,
    val text: String,
    val createdAt: String
)

data class CreateLessonTicketMessageRequest(
    val text: String
)

data class LessonTicketDto(
    val id: Long,
    val teacherId: Long,
    val teacherName: String,
    val title: String,
    val description: String?,
    val status: String,
    val adminNote: String?,
    val createdLessonId: Long?,
    val suggestedCourseId: Long?,
    val chatClosed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<LessonTicketAttachmentDto>
)
