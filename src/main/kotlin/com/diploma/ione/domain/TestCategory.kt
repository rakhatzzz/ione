package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "test_categories")
class TestCategory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    var test: PsychologicalTest,

    @Column(nullable = false)
    var name: String,

    var description: String? = null
)