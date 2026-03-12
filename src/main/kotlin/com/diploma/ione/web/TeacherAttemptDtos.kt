package com.diploma.ione.web

data class TeacherAttemptDetailsDto(
    val attemptId: Long,
    val testId: Long,
    val testTitle: String,
    val studentId: Long,
    val studentName: String,
    val className: String?,
    val isFinished: Boolean,
    val startedAt: String,
    val finishedAt: String?,
    val maxZone: String,
    val categoryResults: List<CategoryResultDto>,
    val answers: List<AttemptAnswerDto>
)

data class AttemptAnswerDto(
    val questionId: Long,
    val orderNumber: Int,
    val categoryId: Long,
    val categoryName: String,
    val questionText: String,
    val selectedOptionId: Long,
    val selectedOptionText: String,
    val score: Int
)