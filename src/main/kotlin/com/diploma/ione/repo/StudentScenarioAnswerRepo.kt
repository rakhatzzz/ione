package com.diploma.ione.repo

import com.diploma.ione.domain.StudentScenarioAnswer
import org.springframework.data.jpa.repository.JpaRepository

interface StudentScenarioAnswerRepo : JpaRepository<StudentScenarioAnswer, Long> {
    fun findByStudentIdAndScenarioId(studentId: Long, scenarioId: Long): StudentScenarioAnswer?
    fun findAllByStudentIdAndScenarioLessonCourseId(studentId: Long, courseId: Long): List<StudentScenarioAnswer>
}