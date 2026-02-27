package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "schools")
class School(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    var address: String? = null
)