package com.diploma.ione.repo

import com.diploma.ione.domain.LessonTicketAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface LessonTicketAttachmentRepo : JpaRepository<LessonTicketAttachment, Long> {
    fun findAllByTicketIdOrderByCreatedAtAsc(ticketId: Long): List<LessonTicketAttachment>

    @Modifying
    @Transactional
    fun deleteAllByTicketId(ticketId: Long)
    fun countByTicketId(ticketId: Long): Long
}
