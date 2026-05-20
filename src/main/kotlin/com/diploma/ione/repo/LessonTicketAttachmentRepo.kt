package com.diploma.ione.repo

import com.diploma.ione.domain.LessonTicketAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface LessonTicketAttachmentRepo : JpaRepository<LessonTicketAttachment, Long> {
    fun findAllByTicketIdOrderByCreatedAtAsc(ticketId: Long): List<LessonTicketAttachment>
    fun deleteAllByTicketId(ticketId: Long)
}
