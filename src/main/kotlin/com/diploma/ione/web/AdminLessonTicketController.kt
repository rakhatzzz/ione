package com.diploma.ione.web

import com.diploma.ione.domain.Lesson
import com.diploma.ione.domain.LessonTicketStatus
import com.diploma.ione.repo.CourseRepo
import com.diploma.ione.repo.LessonRepo
import com.diploma.ione.repo.LessonTicketAttachmentRepo
import com.diploma.ione.repo.LessonTicketRepo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

data class UpdateLessonTicketStatusRequest(
    val status: String,
    val adminNote: String?
)

data class CreateLessonFromTicketRequest(
    val courseId: Long,
    val title: String?,
    val videoPath: String?,
    val textContent: String?,
    val orderNumber: Int?
)

@RestController
@RequestMapping("/api/admin")
class AdminLessonTicketController(
    private val ticketRepo: LessonTicketRepo,
    private val attachmentRepo: LessonTicketAttachmentRepo,
    private val courseRepo: CourseRepo,
    private val lessonRepo: LessonRepo
) {

    @GetMapping("/lesson-tickets")
    fun allTickets(@RequestParam(required = false) status: String?): List<LessonTicketDto> {
        val tickets = if (status.isNullOrBlank()) {
            ticketRepo.findAll().sortedByDescending { it.createdAt }
        } else {
            val st = LessonTicketStatus.valueOf(status.uppercase())
            ticketRepo.findAllByStatusOrderByCreatedAtDesc(st)
        }

        if (tickets.isEmpty()) return emptyList()

        val ticketIds = tickets.mapNotNull { it.id }
        val attachmentsByTicketId = attachmentRepo.findAll().filter { it.ticket.id in ticketIds }.groupBy { it.ticket.id!! }

        return tickets.map { t ->
            val atts = attachmentsByTicketId[t.id!!].orEmpty().map { a ->
                LessonTicketAttachmentDto(
                    id = a.id!!,
                    fileUrl = a.fileUrl,
                    originalName = a.originalName,
                    kind = a.kind.name,
                    createdAt = a.createdAt.toString()
                )
            }
            LessonTicketDto(
                id = t.id!!,
                teacherId = t.teacher.id!!,
                teacherName = t.teacher.user.fullName,
                title = t.title,
                description = t.description,
                status = t.status.name,
                adminNote = t.adminNote,
                createdLessonId = t.createdLessonId,
                suggestedCourseId = t.suggestedCourseId,
                createdAt = t.createdAt.toString(),
                updatedAt = t.updatedAt.toString(),
                attachments = atts
            )
        }
    }

    @PostMapping("/lesson-tickets/{ticketId}/status")
    fun updateStatus(@PathVariable ticketId: Long, @RequestBody req: UpdateLessonTicketStatusRequest): ResponseEntity<Any> {
        val ticket = ticketRepo.findById(ticketId).orElseThrow { error("Ticket not found") }
        val st = LessonTicketStatus.valueOf(req.status.uppercase())

        ticket.status = st
        ticket.adminNote = req.adminNote?.trim()?.takeIf { it.isNotBlank() }
        ticket.updatedAt = LocalDateTime.now()

        val saved = ticketRepo.save(ticket)

        val atts = attachmentRepo.findAllByTicketIdOrderByCreatedAtAsc(saved.id!!).map { a ->
            LessonTicketAttachmentDto(
                id = a.id!!,
                fileUrl = a.fileUrl,
                originalName = a.originalName,
                kind = a.kind.name,
                createdAt = a.createdAt.toString()
            )
        }

        return ResponseEntity.ok(
            LessonTicketDto(
                id = saved.id!!,
                teacherId = saved.teacher.id!!,
                teacherName = saved.teacher.user.fullName,
                title = saved.title,
                description = saved.description,
                status = saved.status.name,
                adminNote = saved.adminNote,
                createdLessonId = saved.createdLessonId,
                suggestedCourseId = saved.suggestedCourseId,
                createdAt = saved.createdAt.toString(),
                updatedAt = saved.updatedAt.toString(),
                attachments = atts
            )
        )
    }

    @PostMapping("/lesson-tickets/{ticketId}/create-lesson")
    fun createLessonFromTicket(@PathVariable ticketId: Long, @RequestBody req: CreateLessonFromTicketRequest): ResponseEntity<Any> {
        val ticket = ticketRepo.findById(ticketId).orElseThrow { error("Ticket not found") }
        val course = courseRepo.findById(req.courseId).orElseThrow { error("Course not found") }

        val lesson = Lesson(
            course = course,
            title = (req.title?.trim()?.takeIf { it.isNotBlank() } ?: ticket.title),
            videoPath = req.videoPath?.trim()?.takeIf { it.isNotBlank() },
            textContent = req.textContent?.trim()?.takeIf { it.isNotBlank() } ?: ticket.description,
            orderNumber = req.orderNumber ?: 1
        )

        val savedLesson = lessonRepo.save(lesson)

        ticket.createdLessonId = savedLesson.id
        ticket.status = LessonTicketStatus.IMPLEMENTED
        ticket.updatedAt = LocalDateTime.now()
        ticketRepo.save(ticket)

        return ResponseEntity.ok(mapOf("lessonId" to savedLesson.id, "ticketId" to ticket.id, "status" to ticket.status.name))
    }
}
