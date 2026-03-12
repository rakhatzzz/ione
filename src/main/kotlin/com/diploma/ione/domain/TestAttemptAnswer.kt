package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "test_attempt_answers",
    uniqueConstraints = [UniqueConstraint(columnNames = ["attempt_id", "question_id"])]
)
class TestAttemptAnswer(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    var attempt: TestAttempt,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    var question: TestQuestion,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", nullable = false)
    var selectedOption: TestAnswerOption,

    @Column(nullable = false)
    var score: Int,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)