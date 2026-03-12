package com.diploma.ione.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "student_scenario_answers",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "scenario_id"])]
)
class StudentScenarioAnswer(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    var scenario: Scenario,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", nullable = false)
    var selectedOption: ScenarioOption,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)