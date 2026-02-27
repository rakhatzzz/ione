package com.diploma.ione.repo

import com.diploma.ione.domain.Teacher
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherRepo : JpaRepository<Teacher, Long>