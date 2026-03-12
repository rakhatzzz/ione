package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "test_questions")
class TestQuestion(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    var test: PsychologicalTest,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: TestCategory,

    @Column(nullable = false, columnDefinition = "text")
    var text: String,

    @Column(name = "order_number", nullable = false)
    var orderNumber: Int = 1
)