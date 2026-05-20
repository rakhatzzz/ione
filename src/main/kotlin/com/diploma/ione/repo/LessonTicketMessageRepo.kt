package com.diploma.ione.repo

import com.diploma.ione.domain.LessonTicketMessage
import org.springframework.data.jpa.repository.JpaRepository

interface LessonTicketMessageRepo : JpaRepository<LessonTicketMessage, Long> {
    fun findAllByTicketIdOrderByCreatedAtAsc(ticketId: Long): List<LessonTicketMessage>
}
