package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "teachers")
class Teacher(
    @Id
    var id: Long? = null, // = user.id

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @MapsId
    @JoinColumn(name = "id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    var school: School
)