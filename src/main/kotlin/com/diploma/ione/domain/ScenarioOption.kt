package com.diploma.ione.domain

import jakarta.persistence.*

@Entity
@Table(name = "scenario_options")
class ScenarioOption(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    var scenario: Scenario,

    @Column(name = "option_text", nullable = false)
    var optionText: String,

    @Column(name = "result_text")
    var resultText: String? = null,

    @Column(name = "result_image_path")
    var resultImagePath: String? = null,

    var score: Int = 0
)