package com.diploma.ione.auth

import com.diploma.ione.domain.Role
import com.diploma.ione.domain.Student
import com.diploma.ione.domain.Teacher
import com.diploma.ione.domain.User
import com.diploma.ione.repo.SchoolRepo
import com.diploma.ione.repo.StudentRepo
import com.diploma.ione.repo.TeacherRepo
import com.diploma.ione.repo.UserRepo
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepo: UserRepo,
    private val schoolRepo: SchoolRepo,
    private val teacherRepo: TeacherRepo,
    private val studentRepo: StudentRepo,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService
) {
    fun login(req: LoginRequest): AuthResponse {
        val user = userRepo.findByEmail(req.email) ?: error("Invalid email or password")
        if (user.passwordHash == null || !encoder.matches(req.password, user.passwordHash)) {
            error("Invalid email or password")
        }
        val token = jwt.generateToken(user.id!!, user.role)
        return AuthResponse(token, user.id!!, user.role.name)
    }

    @Transactional
    fun registerTeacher(req: RegisterTeacherRequest): AuthResponse {
        if (userRepo.existsByEmail(req.email)) error("Email already used")
        val school = schoolRepo.findById(req.schoolId).orElseThrow { error("School not found") }

        val user = userRepo.save(
            User(
                fullName = req.fullName,
                email = req.email,
                passwordHash = encoder.encode(req.password),
                role = Role.TEACHER
            )
        )

        teacherRepo.save(Teacher(user = user, school = school))

        val token = jwt.generateToken(user.id!!, user.role)
        return AuthResponse(token, user.id!!, user.role.name)
    }

    @Transactional
    fun registerStudent(req: RegisterStudentRequest): AuthResponse {
        if (userRepo.existsByEmail(req.email)) error("Email already used")
        val school = schoolRepo.findById(req.schoolId).orElseThrow { error("School not found") }
        val teacher = teacherRepo.findById(req.teacherId).orElseThrow { error("Teacher not found") }

        // защита: ученик должен выбирать учителя той же школы
        if (teacher.school.id != school.id) error("Teacher must be from the same school")

        val user = userRepo.save(
            User(
                fullName = req.fullName,
                email = req.email,
                passwordHash = encoder.encode(req.password),
                role = Role.STUDENT
            )
        )

        studentRepo.save(Student(user = user, school = school, teacher = teacher, className = req.className))

        val token = jwt.generateToken(user.id!!, user.role)
        return AuthResponse(token, user.id!!, user.role.name)
    }
}