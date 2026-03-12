package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "category_zones")
class CategoryZone(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: TestCategory,

    @Enumerated(EnumType.STRING)
    @Column(name = "zone", nullable = false)
    var zone: ZoneType,

    @Column(name = "min_score", nullable = false)
    var minScore: Int,

    @Column(name = "max_score", nullable = false)
    var maxScore: Int,

    @Column(nullable = false)
    var priority: Int = 0
)

enum class ZoneType {
    GREEN, YELLOW, RED, BLACK
}