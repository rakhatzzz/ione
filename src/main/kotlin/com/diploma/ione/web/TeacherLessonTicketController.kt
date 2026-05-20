package com.diploma.ione.web

import com.diploma.ione.auth.AuthUtil
import com.diploma.ione.domain.*
import com.diploma.ione.repo.LessonTicketAttachmentRepo
import com.diploma.ione.repo.LessonTicketRepo
import com.diploma.ione.repo.TeacherRepo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

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
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<LessonTicketAttachmentDto>
)

@RestController
@RequestMapping("/api/teacher")
class TeacherLessonTicketController(
    private val teacherRepo: TeacherRepo,
    private val ticketRepo: LessonTicketRepo,
    private val attachmentRepo: LessonTicketAttachmentRepo
) {
    private val baseMediaDir = File("media")

    init {
        File(baseMediaDir, "ticket-attachments").mkdirs()
    }

    @PostMapping("/lesson-tickets/add")
    fun createTicket(@RequestBody req: CreateLessonTicketRequest): LessonTicketDto {
        val teacherId = AuthUtil.currentUserId()
        val teacher = teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val title = req.title.trim()
        if (title.isBlank()) error("Title cannot be blank")

        val ticket = LessonTicket(
            teacher = teacher,
            title = title,
            description = req.description?.trim()?.takeIf { it.isNotBlank() }
        )

        val saved = ticketRepo.save(ticket)
        return toDto(saved, emptyList())
    }

    @GetMapping("/lesson-tickets/mine")
    fun myTickets(): List<LessonTicketDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val tickets = ticketRepo.findAllByTeacherIdOrderByCreatedAtDesc(teacherId)
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
            toDto(t, atts)
        }
    }

    @PostMapping("/lesson-tickets/{ticketId}/attachments/upload")
    fun uploadAttachment(
        @PathVariable ticketId: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val ticket = ticketRepo.findById(ticketId).orElseThrow { error("Ticket not found") }
        if (ticket.teacher.id != teacherId) error("Forbidden")
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Файл пустой"))

        val originalFilename = file.originalFilename ?: "file"
        val kind = guessKind(originalFilename)

        val safeName = originalFilename.replace("..", "_").replace("/", "_").replace("\\\\", "_")
        val randomFilename = UUID.randomUUID().toString() + "_" + safeName

        val subFolder = "ticket-attachments/$teacherId"
        File(baseMediaDir, subFolder).mkdirs()

        val targetFile = File(baseMediaDir, "$subFolder/$randomFilename")
        file.transferTo(targetFile.absoluteFile)

        val fileUrl = "/media/$subFolder/$randomFilename"

        val att = LessonTicketAttachment(
            ticket = ticket,
            fileUrl = fileUrl,
            originalName = originalFilename,
            kind = kind,
            createdAt = LocalDateTime.now()
        )

        ticket.updatedAt = LocalDateTime.now()
        ticketRepo.save(ticket)

        val saved = attachmentRepo.save(att)
        return ResponseEntity.ok(
            LessonTicketAttachmentDto(
                id = saved.id!!,
                fileUrl = saved.fileUrl,
                originalName = saved.originalName,
                kind = saved.kind.name,
                createdAt = saved.createdAt.toString()
            )
        )
    }

    private fun toDto(t: LessonTicket, attachments: List<LessonTicketAttachmentDto>): LessonTicketDto =
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
            attachments = attachments
        )

    private fun guessKind(filename: String): LessonTicketAttachmentKind {
        val lower = filename.lowercase()
        return when {
            lower.matches(Regex(".*\\.(mp4|avi|mov|mkv|webm)$")) -> LessonTicketAttachmentKind.VIDEO
            lower.matches(Regex(".*\\.(png|jpg|jpeg|gif|webp|bmp|svg)$")) -> LessonTicketAttachmentKind.IMAGE
            lower.matches(Regex(".*\\.(pdf|doc|docx|ppt|pptx|xls|xlsx|txt)$")) -> LessonTicketAttachmentKind.DOCUMENT
            else -> LessonTicketAttachmentKind.OTHER
        }
    }
}
