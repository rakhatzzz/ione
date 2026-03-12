package com.diploma.ione.repo

import com.diploma.ione.domain.ScenarioOption
import org.springframework.data.jpa.repository.JpaRepository

interface ScenarioOptionRepo : JpaRepository<ScenarioOption, Long> {
    fun findAllByScenarioId(scenarioId: Long): List<ScenarioOption>
}