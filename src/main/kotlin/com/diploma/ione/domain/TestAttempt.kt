package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "test_attempts")
class TestAttempt(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    var test: PsychologicalTest,

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "finished_at")
    var finishedAt: LocalDateTime? = null,

    @Column(name = "is_finished", nullable = false)
    var isFinished: Boolean = false
)