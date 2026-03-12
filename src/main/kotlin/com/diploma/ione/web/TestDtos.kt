package com.diploma.ione.web

data class TestListDto(
    val id: Long,
    val title: String,
    val description: String?
)

data class TestQuestionDto(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val text: String,
    val orderNumber: Int,
    val options: List<TestOptionDto>
)

data class TestOptionDto(
    val id: Long,
    val text: String
)

data class StartAttemptResponse(
    val attemptId: Long
)

data class AnswerQuestionRequest(
    val optionId: Long
)

data class CategoryResultDto(
    val categoryId: Long,
    val categoryName: String,
    val totalScore: Int,
    val zone: String
)

data class FinishAttemptResponse(
    val attemptId: Long,
    val results: List<CategoryResultDto>,
    val maxZone: String
)