package com.diploma.ione.domain

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "test_answer_options")
class TestAnswerOption(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var question: TestQuestion,

    @Column(nullable = false, columnDefinition = "text")
    var text: String,

    @Column(nullable = false)
    var score: Int
)