package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "teachers")
class Teacher(
    @Id
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @MapsId
    @JoinColumn(name = "id", nullable = false, unique = true)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    var school: School
)
