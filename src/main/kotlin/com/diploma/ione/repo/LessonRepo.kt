package com.diploma.ione.repo

import com.diploma.ione.domain.Lesson
import org.springframework.data.jpa.repository.JpaRepository

interface LessonRepo : JpaRepository<Lesson, Long> {
    fun findAllByCourseIdOrderByOrderNumberAsc(courseId: Long): List<Lesson>
}