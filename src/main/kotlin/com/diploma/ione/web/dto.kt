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
