package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "psychological_tests")
class PsychologicalTest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String,

    var description: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)