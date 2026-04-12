package com.diploma.ione.repo

import com.diploma.ione.domain.StudentLessonProgress
import org.springframework.data.jpa.repository.JpaRepository

interface StudentLessonProgressRepo : JpaRepository<StudentLessonProgress, Long> {
    fun findByStudentIdAndLessonId(studentId: Long, lessonId: Long): StudentLessonProgress?
    fun findAllByStudentIdAndLessonCourseId(studentId: Long, courseId: Long): List<StudentLessonProgress>
}
