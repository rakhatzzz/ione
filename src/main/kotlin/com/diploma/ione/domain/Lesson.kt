package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "lessons")
class Lesson(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    var course: Course,

    @Column(nullable = false)
    var title: String,

    @Column(name = "video_path")
    var videoPath: String? = null,

    @Column(name = "text_content")
    var textContent: String? = null,

    @Column(name = "order_number", nullable = false)
    var orderNumber: Int = 1
)