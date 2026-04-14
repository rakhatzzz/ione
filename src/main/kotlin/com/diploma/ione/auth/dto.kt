package com.diploma.ione.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class AuthResponse(
    val accessToken: String,
    val userId: Long,
    val role: String,
    val fullName: String,
    val studentId: Long? = null,  // Only for STUDENT role
    val teacherFullName: String? = null,
    val className: String? = null,
    val schoolName: String? = null,
    val homeroomClass: String? = null
)

data class RegisterTeacherRequest(
    @field:NotBlank val fullName: String,
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val homeroomClass: String,
    @field:NotNull val schoolId: Long
)

data class RegisterStudentRequest(
    @field:NotBlank val fullName: String,
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotNull val schoolId: Long,
    @field:NotBlank val className: String
)

data class RegisterAdminRequest(
    @field:NotBlank val fullName: String,
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)