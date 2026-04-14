package com.diploma.ione.repo

import com.diploma.ione.domain.Teacher
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherRepo : JpaRepository<Teacher, Long> {
    fun existsBySchoolIdAndHomeroomClass(schoolId: Long, homeroomClass: String): Boolean
    fun existsBySchoolIdAndHomeroomClassAndIdNot(schoolId: Long, homeroomClass: String, id: Long): Boolean
    fun findBySchoolIdAndHomeroomClass(schoolId: Long, homeroomClass: String): Teacher?
}