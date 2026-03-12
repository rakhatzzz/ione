package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "scenarios")
class Scenario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    var lesson: Lesson,

    var title: String? = null,
    var description: String? = null,

    @Column(name = "base_image_path")
    var baseImagePath: String? = null
)