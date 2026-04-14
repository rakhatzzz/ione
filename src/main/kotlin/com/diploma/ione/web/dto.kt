package com.diploma.ione.web

data class CourseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val ageGroup: String?
)

data class LessonDto(
    val id: Long,
    val courseId: Long,
    val title: String,
    val orderNumber: Int,
    val videoUrl: String?,
    val textContent: String?
)

data class StudentLessonProgressDto(
    val lessonId: Long,
    val status: String,
    val completedAt: String?
)

data class StudentCourseProgressDto(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val completed: Boolean
)

data class StudentScenarioOptionDto(
    val id: Long,
    val optionText: String
)

data class StudentLessonScenarioDto(
    val available: Boolean,
    val completed: Boolean = false,
    /** false, если для урока вообще нет сценария в системе */
    val hasScenario: Boolean = false,
    val scenarioId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val baseImageUrl: String? = null,
    val options: List<StudentScenarioOptionDto> = emptyList(),
    /** Пояснение, почему тест недоступен (урок не завершён и т.п.) */
    val message: String? = null,
    val selectedOptionText: String? = null,
    val resultText: String? = null,
    val resultImageUrl: String? = null
)

data class StudentLessonScenariosDto(
    /** false, если для урока вообще нет сценариев в системе */
    val hasScenarios: Boolean = false,
    val scenarios: List<StudentLessonScenarioDto> = emptyList()
)
