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
        val userId = user.id!!
        return when (user.role) {)
            Role.STUDENT -> {
                val student = studentRepo.findById(userId).orElse(null)
                AuthResponse(
                    accessToken = token,
                    userId = userId,
                    role = user.role.name,
                    fullName = user.fullName,
                    studentId = student?.id,
                    teacherFullName = student?.teacher?.user?.fullName,
                    className = student?.className,
                    schoolName = student?.school?.name
                )
            }
            Role.TEACHER -> {
                val teacher = teacherRepo.findById(userId).orElse(null)
                AuthResponse(
                    accessToken = token,
                    userId = userId,
                    role = user.role.name,
                    fullName = user.fullName,
                    schoolName = teacher?.school?.name,
                    homeroomClass = teacher?.homeroomClass
                )
            }
            else -> AuthResponse(token, userId, user.role.name, user.fullName)
        }
    }

    @Transactional
    fun registerTeacher(req: RegisterTeacherRequest): AuthResponse {
        if (userRepo.existsByEmail(req.email)) error("Email already used")
        val school = schoolRepo.findById(req.schoolId).orElseThrow { error("School not found") }
        val normalizedClass = req.homeroomClass.trim().uppercase().replace("\\s+".toRegex(), "")
        if (teacherRepo.existsBySchoolIdAndHomeroomClass(school.id!!, normalizedClass)) {
            error("Этот класс уже закреплён за другим учителем в этой школе")
        }

        val user = User(
            fullName = req.fullName,
            email = req.email,
            passwordHash = encoder.encode(req.password),
            role = Role.TEACHER
        )

        val teacher = Teacher(
            user = user,
            homeroomClass = normalizedClass,
            school = school
        )

        // Save teacher - cascade=PERSIST will save user automatically
        teacherRepo.saveAndFlush(teacher)

        val userId = user.id ?: error("Failed to save user")
        val token = jwt.generateToken(userId, user.role)
        return AuthResponse(
            accessToken = token,
            userId = userId,
            role = user.role.name,
            fullName = user.fullName,
            schoolName = school.name,
            homeroomClass = normalizedClass
        )
    }

    @Transactional
    fun registerStudent(req: RegisterStudentRequest): AuthResponse {
        if (userRepo.existsByEmail(req.email)) error("Email already used")
        val school = schoolRepo.findById(req.schoolId).orElseThrow { error("School not found") }
        val normalizedClass = req.className.trim().uppercase().replace("\\s+".toRegex(), "")
        if (normalizedClass.isBlank()) error("Class name cannot be blank")

        val teacher =
            teacherRepo.findBySchoolIdAndHomeroomClass(school.id!!, normalizedClass)
                ?: error("Для выбранного класса не найден классный руководитель")

        val user = User(
            fullName = req.fullName,
            email = req.email,
            passwordHash = encoder.encode(req.password),
            role = Role.STUDENT
        )

        val student = Student(
            user = user,
            school = school,
            teacher = teacher,
            className = normalizedClass
        )

        // Save student - cascade=PERSIST will save user automatically
        studentRepo.saveAndFlush(student)

        val userId = user.id ?: error("Failed to save user")
        val studentId = student.id ?: error("Failed to save student")
        val token = jwt.generateToken(userId, user.role)
        return AuthResponse(
            accessToken = token,
            userId = userId,
            role = user.role.name,
            fullName = user.fullName,
            studentId = studentId,
            teacherFullName = teacher.user.fullName,
            className = normalizedClass,
            schoolName = school.name
        )
    }

    @Transactional
    fun registerAdmin(req: RegisterAdminRequest): AuthResponse {
        if (userRepo.existsByEmail(req.email)) error("Email already used")

        val user = userRepo.save(
            User(
                fullName = req.fullName,
                email = req.email,
                passwordHash = encoder.encode(req.password),
                role = Role.ADMIN
            )
        )

        val token = jwt.generateToken(user.id!!, user.role)
        return AuthResponse(token, user.id!!, user.role.name, user.fullName)
    }
}