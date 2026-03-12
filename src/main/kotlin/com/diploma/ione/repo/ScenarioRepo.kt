package com.diploma.ione.repo

import com.diploma.ione.domain.Scenario
import org.springframework.data.jpa.repository.JpaRepository

interface ScenarioRepo : JpaRepository<Scenario, Long>