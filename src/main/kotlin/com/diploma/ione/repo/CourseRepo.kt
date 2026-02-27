package com.diploma.ione.repo

import com.diploma.ione.domain.Course
import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepo : JpaRepository<Course, Long> {
}