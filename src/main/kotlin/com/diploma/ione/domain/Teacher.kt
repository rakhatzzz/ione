package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "teachers",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_teachers_school_homeroom_class", columnNames = ["school_id", "homeroom_class"])
    ]
)
class Teacher(
    @Id
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @MapsId
    @JoinColumn(name = "id", nullable = false, unique = true)
    var user: User,

    @Column(name = "homeroom_class", length = 16)
    var homeroomClass: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    var school: School
)
