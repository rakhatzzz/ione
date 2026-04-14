package com.diploma.ione.repo

import com.diploma.ione.domain.Scenario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ScenarioRepo : JpaRepository<Scenario, Long> {
    fun findAllByLessonId(lessonId: Long): List<Scenario>
    fun findAllByLessonIdIn(lessonIds: List<Long>): List<Scenario>
    fun findFirstByLessonIdOrderByIdAsc(lessonId: Long): Scenario?
}
