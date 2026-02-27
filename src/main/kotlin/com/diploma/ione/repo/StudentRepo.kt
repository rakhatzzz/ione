package com.diploma.ione.repo

import com.diploma.ione.domain.Student
import org.springframework.data.jpa.repository.JpaRepository

interface StudentRepo : JpaRepository<Student, Long> {
}