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