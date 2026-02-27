package com.diploma.ione.auth

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody @Valid req: LoginRequest) = authService.login(req)

    @PostMapping("/register/teacher")
    fun registerTeacher(@RequestBody @Valid req: RegisterTeacherRequest) = authService.registerTeacher(req)

    @PostMapping("/register/student")
    fun registerStudent(@RequestBody @Valid req: RegisterStudentRequest) = authService.registerStudent(req)
}