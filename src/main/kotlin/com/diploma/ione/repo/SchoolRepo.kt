package com.diploma.ione.repo

import com.diploma.ione.domain.School
import org.springframework.data.jpa.repository.JpaRepository

interface SchoolRepo : JpaRepository<School, Long>