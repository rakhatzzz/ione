package com.diploma.ione.repo

import com.diploma.ione.domain.LessonTicket
import com.diploma.ione.domain.LessonTicketStatus
import org.springframework.data.jpa.repository.JpaRepository

interface LessonTicketRepo : JpaRepository<LessonTicket, Long> {
    fun findAllByTeacherIdOrderByCreatedAtDesc(teacherId: Long): List<LessonTicket>
    fun findAllByStatusOrderByCreatedAtDesc(status: LessonTicketStatus): List<LessonTicket>
}
