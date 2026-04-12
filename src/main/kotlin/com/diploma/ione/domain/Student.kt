package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "students")
class Student(
    @Id
    var id: Long? = null, // = user.id

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @MapsId
    @JoinColumn(name = "id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    var school: School,

    // классный руководитель
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    var teacher: Teacher,

    @Column(name = "class_name")
    var className: String? = null
)