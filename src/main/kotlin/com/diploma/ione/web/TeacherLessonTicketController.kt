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
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<LessonTicketAttachmentDto>
)

@RestController
@RequestMapping("/api/teacher")
class TeacherLessonTicketController(
    private val teacherRepo: TeacherRepo,
    private val ticketRepo: LessonTicketRepo,
    private val attachmentRepo: LessonTicketAttachmentRepo,
    private val messageRepo: LessonTicketMessageRepo
) {
    companion object {
        const val MULTIPART_FORM = "multipart/form-data"
    }

    @GetMapping("/lesson-tickets/{ticketId}/chat")
    fun myTicketChat(@PathVariable ticketId: Long): List<LessonTicketMessageDto> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val ticket = ticketRepo.findById(ticketId).orElseThrow { error("Ticket not found") }
        if (ticket.teacher.id != teacherId) error("Forbidden")

        return messageRepo.findAllByTicketIdOrderByCreatedAtAsc(ticketId).map { m ->
            LessonTicketMessageDto(
                id = m.id!!,
                sender = m.sender.name,
                senderUserId = m.senderUserId,
                text = m.text,
                createdAt = m.createdAt.toString()
            )
        }
    }

    @PostMapping("/lesson-tickets/{ticketId}/chat/send")
    fun sendChatMessage(@PathVariable ticketId: Long, @RequestBody req: CreateLessonTicketMessageRequest): ResponseEntity<Any> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val ticket = ticketRepo.findById(ticketId).orElseThrow { error("Ticket not found") }
        if (ticket.teacher.id != teacherId) error("Forbidden")
        if (ticket.chatClosed) error("Chat is closed")

        val text = req.text.trim()
        if (text.isBlank()) error("Text cannot be blank")

        val msg = messageRepo.save(
            LessonTicketMessage(
                ticket = ticket,
                sender = LessonTicketMessageSender.TEACHER,
                senderUserId = teacherId,
                text = text,
                createdAt = LocalDateTime.now()
            )
        )

        ticket.updatedAt = LocalDateTime.now()
        ticketRepo.save(ticket)

        return ResponseEntity.ok(
            LessonTicketMessageDto(
                id = msg.id!!,
                sender = msg.sender.name,
                senderUserId = msg.senderUserId,
                text = msg.text,
                createdAt = msg.createdAt.toString()
            )
        )
    }

    private val baseMediaDir = File("media")
    private val maxAttachmentsPerTicket = 10
    private val maxAttachmentBytes = 25L * 1024L * 1024L

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

    @PostMapping("/lesson-tickets/create-with-attachment", consumes = [MULTIPART_FORM])
    fun createTicketWithAttachment(
        @RequestPart("ticket") ticket: CreateLessonTicketRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): LessonTicketDto {
        val teacherId = AuthUtil.currentUserId()
        val teacher = teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val t = ticket.title.trim()
        if (t.isBlank()) error("Title cannot be blank")

        val description = ticket.description?.trim()?.takeIf { it.isNotBlank() }

        val savedTicket = ticketRepo.save(LessonTicket(
            teacher = teacher,
            title = t,
            description = description
        ))

        val attachments = mutableListOf<LessonTicketAttachmentDto>()

        if (file != null && !file.isEmpty) {
            if (file.size > maxAttachmentBytes) error("File too large. Max 25MB")
            val existingCount = attachmentRepo.countByTicketId(savedTicket.id!!).toInt()
            if (existingCount >= maxAttachmentsPerTicket) error("Too many attachments. Max 10")

            val originalFilename = file.originalFilename ?: "file"
            val kind = guessKind(originalFilename)

            val safeName = originalFilename
                .replace("..", "_")
                .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .take(120)
            val randomFilename = UUID.randomUUID().toString() + "_" + safeName

            val subFolder = "ticket-attachments/$teacherId"
            File(baseMediaDir, subFolder).mkdirs()

            val targetFile = File(baseMediaDir, "$subFolder/$randomFilename")
            file.transferTo(targetFile.absoluteFile)

            val fileUrl = "/media/$subFolder/$randomFilename"

            val att = LessonTicketAttachment(
                ticket = savedTicket,
                fileUrl = fileUrl,
                originalName = originalFilename,
                kind = kind,
                createdAt = LocalDateTime.now()
            )

            savedTicket.updatedAt = LocalDateTime.now()
            ticketRepo.save(savedTicket)

            val savedAtt = attachmentRepo.save(att)
            attachments.add(
                LessonTicketAttachmentDto(
                    id = savedAtt.id!!,
                    fileUrl = savedAtt.fileUrl,
                    originalName = savedAtt.originalName,
                    kind = savedAtt.kind.name,
                    createdAt = savedAtt.createdAt.toString()
                )
            )
        }

        return toDto(savedTicket, attachments)
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
        if (file.size > maxAttachmentBytes) error("File too large. Max 25MB")

        val existingCount = attachmentRepo.countByTicketId(ticketId).toInt()
        if (existingCount >= maxAttachmentsPerTicket) error("Too many attachments. Max 10")
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Файл пустой"))

        val originalFilename = file.originalFilename ?: "file"
        val kind = guessKind(originalFilename)

        val safeName = originalFilename
            .replace("..", "_")
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(120)
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

    @PostMapping("/lesson-tickets/{ticketId}/delete")
    fun deleteMyTicket(@PathVariable ticketId: Long): ResponseEntity<Any> {
        val teacherId = AuthUtil.currentUserId()
        teacherRepo.findById(teacherId).orElseThrow { error("Teacher not found") }

        val ticket = ticketRepo.findById(ticketId).orElseThrow { error("Ticket not found") }
        if (ticket.teacher.id != teacherId) error("Forbidden")

        val attachments = attachmentRepo.findAllByTicketIdOrderByCreatedAtAsc(ticketId)
        attachments.forEach { a ->
            deleteMediaIfLocal(a.fileUrl)
        }
        attachmentRepo.deleteAllByTicketId(ticketId)
        ticketRepo.delete(ticket)

        return ResponseEntity.ok(mapOf("success" to true))
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

    private fun deleteMediaIfLocal(fileUrl: String) {
        val normalized = fileUrl.trim()
        if (!normalized.startsWith("/media/")) return

        val rel = normalized.removePrefix("/media/").replace("..", "")
        val f = File(baseMediaDir, rel)
        try {
            val basePath = baseMediaDir.canonicalFile.toPath()
            val filePath = f.canonicalFile.toPath()
            if (!filePath.startsWith(basePath)) return
            if (f.exists() && f.isFile) {
                f.delete()
            }
        } catch (_: Exception) {
            // ignore
        }
    }
}
