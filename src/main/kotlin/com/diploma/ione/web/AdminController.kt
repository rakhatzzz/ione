package com.diploma.ione.web

import com.diploma.ione.domain.Course
import com.diploma.ione.domain.Lesson
import com.diploma.ione.domain.PsychologicalTest
import com.diploma.ione.domain.School
import com.diploma.ione.domain.TestCategory
import com.diploma.ione.domain.TestQuestion
import com.diploma.ione.domain.TestAnswerOption
import com.diploma.ione.domain.CategoryZone
import com.diploma.ione.domain.ZoneType
import com.diploma.ione.repo.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminDashboardDto(
        val schools: List<AdminSchoolDto>,
        val courses: List<AdminCourseDto>,
        val tests: List<AdminTestDto>,
        val scenarios: List<AdminScenarioDto>
)

data class AdminSchoolDto(val id: Long, val name: String, val teachers: List<AdminTeacherDto>)

data class AdminTeacherDto(val id: Long, val fullName: String, val students: List<AdminStudentDto>)

data class AdminStudentDto(val id: Long, val fullName: String, val className: String?)

data class AdminCourseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val ageGroup: String?,
    val lessons: List<AdminLessonDto>
)

data class AdminLessonDto(
    val id: Long,
    val title: String,
    val orderNumber: Int,
    val videoPath: String?,
    val textContent: String?
)

data class AdminTestDto(
    val id: Long,
    val title: String,
    val description: String?,
    val categories: List<AdminTestCategoryDto>
)

data class AdminTestCategoryDto(
    val id: Long,
    val name: String,
    val description: String?,
    val zones: List<AdminCategoryZoneDto>,
    val questions: List<AdminTestQuestionDto>
)

data class AdminCategoryZoneDto(
    val id: Long,
    val zone: String,
    val minScore: Int,
    val maxScore: Int,
    val priority: Int
)

data class AdminTestQuestionDto(
    val id: Long,
    val text: String,
    val orderNumber: Int,
    val answers: List<AdminTestAnswerDto>
)

data class AdminTestAnswerDto(
    val id: Long,
    val text: String,
    val score: Int
)

data class AdminScenarioDto(
    val id: Long,
    val lessonId: Long,
    val title: String,
    val description: String?,
    val baseImagePath: String?,
    val options: List<AdminScenarioOptionDto>
)

data class AdminScenarioOptionDto(
    val id: Long,
    val optionText: String,
    val resultText: String,
    val resultImagePath: String?,
    val score: Int
)

data class CreateSchoolRequest(val name: String, val address: String?)

data class UpdateSchoolRequest(val name: String?, val address: String?)

data class UpdateTeacherRequest(val fullName: String?)

data class UpdateStudentRequest(val fullName: String?, val className: String?)

data class CreateCourseRequest(val title: String, val description: String?, val ageGroup: String?)

data class UpdateCourseRequest(val title: String?, val description: String?, val ageGroup: String?)

data class CreateLessonRequest(
    val courseId: Long,
    val title: String,
    val videoPath: String?,
    val textContent: String?,
    val orderNumber: Int = 1
)

data class UpdateLessonRequest(
    val title: String?,
    val videoPath: String?,
    val textContent: String?,
    val orderNumber: Int?
)

data class CreateTestRequest(val title: String, val description: String?)
data class UpdateTestRequest(val title: String?, val description: String?)

data class CreateCategoryRequest(val testId: Long, val name: String, val description: String?)
data class UpdateCategoryRequest(val name: String?, val description: String?)

data class CreateCategoryZoneRequest(val categoryId: Long, val zone: String, val minScore: Int, val maxScore: Int, val priority: Int)
data class UpdateCategoryZoneRequest(val zone: String?, val minScore: Int?, val maxScore: Int?, val priority: Int?)

data class CreateTestQuestionRequest(val testId: Long, val categoryId: Long, val text: String, val orderNumber: Int)
data class UpdateTestQuestionRequest(val text: String?, val orderNumber: Int?)

data class CreateTestAnswerRequest(val questionId: Long, val text: String, val score: Int)
data class UpdateTestAnswerRequest(val text: String?, val score: Int?)

@RestController
@RequestMapping("/api/admin")
class AdminController(
        private val userRepo: UserRepo,
        private val schoolRepo: SchoolRepo,
        private val teacherRepo: TeacherRepo,
        private val studentRepo: StudentRepo,
        private val courseRepo: CourseRepo,
        private val lessonRepo: LessonRepo,
        private val testRepo: PsychologicalTestRepo,
        private val categoryRepo: TestCategoryRepo,
        private val zoneRepo: CategoryZoneRepo,
        private val questionRepo: TestQuestionRepo,
        private val answerRepo: TestAnswerOptionRepo,
        private val scenarioRepo: ScenarioRepo,
        private val scenarioOptionRepo: ScenarioOptionRepo
) {
    @GetMapping("/dashboard")
    fun getDashboardData(): AdminDashboardDto {
        val schools = schoolRepo.findAll()
        val teachers = teacherRepo.findAll()
        val students = studentRepo.findAll()

        // Map teachers by school
        val teachersBySchool = teachers.groupBy { it.school.id }
        // Map students by teacher
        val studentsByTeacher = students.groupBy { it.teacher.id }

        val schoolDtos =
                schools.map { school ->
                    val schoolTeachers = teachersBySchool[school.id] ?: emptyList()
                    val teacherDtos =
                            schoolTeachers.map { teacher ->
                                val teacherStudents = studentsByTeacher[teacher.id] ?: emptyList()
                                val studentDtos =
                                        teacherStudents.map { student ->
                                            AdminStudentDto(
                                                    id = student.id!!,
                                                    fullName = student.user.fullName,
                                                    className = student.className
                                            )
                                        }
                                AdminTeacherDto(
                                        id = teacher.id!!,
                                        fullName = teacher.user.fullName,
                                        students = studentDtos
                                )
                            }
                    AdminSchoolDto(id = school.id!!, name = school.name, teachers = teacherDtos)
                }

        val allLessons = lessonRepo.findAll().groupBy { it.course.id }
        val courses = courseRepo.findAll().map { course ->
            val nestedLessons = (allLessons[course.id] ?: emptyList())
                .sortedBy { it.orderNumber }
                .map {
                    AdminLessonDto(
                        id = it.id!!,
                        title = it.title,
                        orderNumber = it.orderNumber,
                        videoPath = it.videoPath,
                        textContent = it.textContent
                    )
                }
            AdminCourseDto(course.id!!, course.title, course.description, course.ageGroup, nestedLessons)
        }
        val allCategories = categoryRepo.findAll().groupBy { it.test.id }
        val allZones = zoneRepo.findAll().groupBy { it.category.id }
        val allQuestions = questionRepo.findAll().groupBy { it.category.id }
        val allAnswers = answerRepo.findAll().groupBy { it.question.id }

        val tests = testRepo.findAll().map { test ->
            val nestedCategories = (allCategories[test.id] ?: emptyList()).map { cat ->
                val zones = (allZones[cat.id] ?: emptyList()).sortedBy { it.priority }.map { z ->
                    AdminCategoryZoneDto(z.id!!, z.zone.name, z.minScore, z.maxScore, z.priority)
                }
                val questions = (allQuestions[cat.id] ?: emptyList()).sortedBy { it.orderNumber }.map { q ->
                    val answers = (allAnswers[q.id] ?: emptyList()).map { a ->
                        AdminTestAnswerDto(a.id!!, a.text, a.score)
                    }
                    AdminTestQuestionDto(q.id!!, q.text, q.orderNumber, answers)
                }
                AdminTestCategoryDto(cat.id!!, cat.name, cat.description, zones, questions)
            }
            AdminTestDto(test.id!!, test.title, test.description, nestedCategories)
        }
        val allScenarioOptions = scenarioOptionRepo.findAll().groupBy { it.scenario.id }
        val scenarios =
                scenarioRepo.findAll().map { s ->
                    val opts = (allScenarioOptions[s.id] ?: emptyList()).map { o ->
                        AdminScenarioOptionDto(
                            id = o.id!!, 
                            optionText = o.optionText ?: "", 
                            resultText = o.resultText ?: "", 
                            resultImagePath = o.resultImagePath, 
                            score = o.score
                        )
                    }
                    AdminScenarioDto(
                        id = s.id!!,
                        lessonId = s.lesson.id!!,
                        title = s.title ?: "Unnamed Scenario",
                        description = s.description,
                        baseImagePath = s.baseImagePath,
                        options = opts
                    )
                }

        return AdminDashboardDto(
                schools = schoolDtos,
                courses = courses,
                tests = tests,
                scenarios = scenarios
        )
    }

    // --- SCHOOLS CRUD ---

    @PostMapping("/schools/add")
    fun createSchool(@RequestBody req: CreateSchoolRequest): AdminSchoolDto {
        // Предполагается, что в сущности School есть поля name и address
        val school = School(name = req.name, address = req.address)
        val saved = schoolRepo.save(school)
        return AdminSchoolDto(saved.id!!, saved.name, emptyList())
    }

    @PostMapping("/schools/update/{id}")
    fun updateSchool(@org.springframework.web.bind.annotation.PathVariable id: Long, @RequestBody req: UpdateSchoolRequest): AdminSchoolDto {
        val school = schoolRepo.findById(id).orElseThrow { error("School not found") }
        if (req.name != null) school.name = req.name
        if (req.address != null) school.address = req.address
        val saved = schoolRepo.save(school)
        return AdminSchoolDto(saved.id!!, saved.name, emptyList()) // Возвращаем пустой список учителей для простоты ответа
    }

    @PostMapping("/schools/delete/{id}")
    fun deleteSchool(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        schoolRepo.deleteById(id)
    }

    // --- TEACHERS CRUD ---

    @PostMapping("/teachers/update/{id}")
    fun updateTeacher(@org.springframework.web.bind.annotation.PathVariable id: Long, @RequestBody req: UpdateTeacherRequest): AdminTeacherDto {
        val teacher = teacherRepo.findById(id).orElseThrow { error("Teacher not found") }
        req.fullName?.let {
            teacher.user.fullName = it
            userRepo.save(teacher.user)
        }
        return AdminTeacherDto(teacher.id!!, teacher.user.fullName, emptyList())
    }

    @PostMapping("/teachers/delete/{id}")
    fun deleteTeacher(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        teacherRepo.deleteById(id)
        userRepo.deleteById(id)
    }

    // --- STUDENTS CRUD ---

    @PostMapping("/students/update/{id}")
    fun updateStudent(@org.springframework.web.bind.annotation.PathVariable id: Long, @RequestBody req: UpdateStudentRequest): AdminStudentDto {
        val student = studentRepo.findById(id).orElseThrow { error("Student not found") }
        req.fullName?.let {
            student.user.fullName = it
            userRepo.save(student.user)
        }
        req.className?.let { student.className = it }
        studentRepo.save(student)
        return AdminStudentDto(student.id!!, student.user.fullName, student.className)
    }

    @PostMapping("/students/delete/{id}")
    fun deleteStudent(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        studentRepo.deleteById(id)
        userRepo.deleteById(id)
    }

    @PostMapping("/courses/add")
    fun createCourse(@RequestBody req: CreateCourseRequest): AdminCourseDto {
        val course =
                Course(title = req.title, description = req.description, ageGroup = req.ageGroup)
        val saved = courseRepo.save(course)
        return AdminCourseDto(saved.id!!, saved.title, saved.description, saved.ageGroup, emptyList())
    }

    @PostMapping("/courses/update/{id}")
    fun updateCourse(
            @org.springframework.web.bind.annotation.PathVariable id: Long,
            @RequestBody req: UpdateCourseRequest
    ): AdminCourseDto {
        val course = courseRepo.findById(id).orElseThrow { error("Course not found") }
        if (req.title != null) course.title = req.title
        if (req.description != null) course.description = req.description
        if (req.ageGroup != null) course.ageGroup = req.ageGroup
        val saved = courseRepo.save(course)
        val lessons = lessonRepo.findAllByCourseIdOrderByOrderNumberAsc(saved.id!!)
            .map { AdminLessonDto(it.id!!, it.title, it.orderNumber, it.videoPath, it.textContent) }
        return AdminCourseDto(saved.id!!, saved.title, saved.description, saved.ageGroup, lessons)
    }

    @PostMapping("/courses/delete/{id}")
    fun deleteCourse(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val course = courseRepo.findById(id).orElseThrow { error("Course not found") }
        courseRepo.delete(course)
    }

    @PostMapping("/lessons/add")
    fun createLesson(@RequestBody req: CreateLessonRequest): AdminLessonDto {
        val course = courseRepo.findById(req.courseId).orElseThrow { error("Course not found") }
        val lesson = Lesson(
            course = course,
            title = req.title,
            videoPath = req.videoPath,
            textContent = req.textContent,
            orderNumber = req.orderNumber
        )
        val saved = lessonRepo.save(lesson)
        return AdminLessonDto(saved.id!!, saved.title, saved.orderNumber, saved.videoPath, saved.textContent)
    }

    @PostMapping("/lessons/update/{id}")
    fun updateLesson(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateLessonRequest
    ): AdminLessonDto {
        val lesson = lessonRepo.findById(id).orElseThrow { error("Lesson not found") }
        if (req.title != null) lesson.title = req.title
        if (req.videoPath != null) lesson.videoPath = req.videoPath
        if (req.textContent != null) lesson.textContent = req.textContent
        if (req.orderNumber != null) lesson.orderNumber = req.orderNumber
        val saved = lessonRepo.save(lesson)
        return AdminLessonDto(saved.id!!, saved.title, saved.orderNumber, saved.videoPath, saved.textContent)
    }

    @PostMapping("/lessons/delete/{id}")
    fun deleteLesson(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val lesson = lessonRepo.findById(id).orElseThrow { error("Lesson not found") }
        lessonRepo.delete(lesson)
    }

    // --- PSYCHOLOGICAL TESTS CRUD ---

    @PostMapping("/tests/add")
    fun createTest(@RequestBody req: CreateTestRequest): AdminTestDto {
        val test = PsychologicalTest(title = req.title, description = req.description)
        val saved = testRepo.save(test)
        return AdminTestDto(saved.id!!, saved.title, saved.description, emptyList())
    }

    @PostMapping("/tests/update/{id}")
    fun updateTest(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateTestRequest
    ): AdminTestDto {
        val test = testRepo.findById(id).orElseThrow { error("Test not found") }
        if (req.title != null) test.title = req.title
        if (req.description != null) test.description = req.description
        val saved = testRepo.save(test)
        
        // Return fully populated (or just shallow, since frontend updates optimistic)
        val categories = categoryRepo.findAllByTestId(saved.id!!).map { cat ->
            val zones = zoneRepo.findAllByCategoryId(cat.id!!).map { z -> AdminCategoryZoneDto(z.id!!, z.zone.name, z.minScore, z.maxScore, z.priority) }
            val questions = questionRepo.findAllByTestIdOrderByOrderNumberAsc(saved.id!!).filter { it.category.id == cat.id }.map { q ->
                val answers = answerRepo.findAllByQuestionId(q.id!!).map { a -> AdminTestAnswerDto(a.id!!, a.text, a.score) }
                AdminTestQuestionDto(q.id!!, q.text, q.orderNumber, answers)
            }
            AdminTestCategoryDto(cat.id!!, cat.name, cat.description, zones, questions)
        }
        return AdminTestDto(saved.id!!, saved.title, saved.description, categories)
    }

    @PostMapping("/tests/delete/{id}")
    fun deleteTest(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val test = testRepo.findById(id).orElseThrow { error("Test not found") }
        testRepo.delete(test)
    }

    // --- TEST CATEGORIES ---

    @PostMapping("/test-categories/add")
    fun createCategory(@RequestBody req: CreateCategoryRequest): AdminTestCategoryDto {
        val test = testRepo.findById(req.testId).orElseThrow { error("Test not found") }
        val category = TestCategory(test = test, name = req.name, description = req.description)
        val saved = categoryRepo.save(category)
        return AdminTestCategoryDto(saved.id!!, saved.name, saved.description, emptyList(), emptyList())
    }

    @PostMapping("/test-categories/update/{id}")
    fun updateCategory(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateCategoryRequest
    ): AdminTestCategoryDto {
        val cat = categoryRepo.findById(id).orElseThrow { error("Category not found") }
        if (req.name != null) cat.name = req.name
        if (req.description != null) cat.description = req.description
        val saved = categoryRepo.save(cat)
        
        val zones = zoneRepo.findAllByCategoryId(saved.id!!).sortedBy { it.priority }.map { z -> AdminCategoryZoneDto(z.id!!, z.zone.name, z.minScore, z.maxScore, z.priority) }
        val questions = questionRepo.findAllByTestIdOrderByOrderNumberAsc(saved.test.id!!).filter { it.category.id == saved.id }.map { q ->
            val answers = answerRepo.findAllByQuestionId(q.id!!).map { a -> AdminTestAnswerDto(a.id!!, a.text, a.score) }
            AdminTestQuestionDto(q.id!!, q.text, q.orderNumber, answers)
        }
        return AdminTestCategoryDto(saved.id!!, saved.name, saved.description, zones, questions)
    }

    @PostMapping("/test-categories/delete/{id}")
    fun deleteCategory(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val cat = categoryRepo.findById(id).orElseThrow { error("Category not found") }
        categoryRepo.delete(cat)
    }

    // --- CATEGORY ZONES ---

    @PostMapping("/category-zones/add")
    fun createCategoryZone(@RequestBody req: CreateCategoryZoneRequest): AdminCategoryZoneDto {
        val cat = categoryRepo.findById(req.categoryId).orElseThrow { error("Category not found") }
        val zoneType = try { ZoneType.valueOf(req.zone.uppercase()) } catch(e: Exception) { ZoneType.GREEN }
        val zone = CategoryZone(category = cat, zone = zoneType, minScore = req.minScore, maxScore = req.maxScore, priority = req.priority)
        val saved = zoneRepo.save(zone)
        return AdminCategoryZoneDto(saved.id!!, saved.zone.name, saved.minScore, saved.maxScore, saved.priority)
    }

    @PostMapping("/category-zones/update/{id}")
    fun updateCategoryZone(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateCategoryZoneRequest
    ): AdminCategoryZoneDto {
        val zone = zoneRepo.findById(id).orElseThrow { error("Zone not found") }
        if (req.zone != null) zone.zone = try { ZoneType.valueOf(req.zone.uppercase()) } catch(e: Exception) { zone.zone }
        if (req.minScore != null) zone.minScore = req.minScore
        if (req.maxScore != null) zone.maxScore = req.maxScore
        if (req.priority != null) zone.priority = req.priority
        val saved = zoneRepo.save(zone)
        return AdminCategoryZoneDto(saved.id!!, saved.zone.name, saved.minScore, saved.maxScore, saved.priority)
    }

    @PostMapping("/category-zones/delete/{id}")
    fun deleteCategoryZone(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val zone = zoneRepo.findById(id).orElseThrow { error("Zone not found") }
        zoneRepo.delete(zone)
    }

    // --- TEST QUESTIONS ---

    @PostMapping("/test-questions/add")
    fun createQuestion(@RequestBody req: CreateTestQuestionRequest): AdminTestQuestionDto {
        val test = testRepo.findById(req.testId).orElseThrow { error("Test not found") }
        val cat = categoryRepo.findById(req.categoryId).orElseThrow { error("Category not found") }
        val q = TestQuestion(test = test, category = cat, text = req.text, orderNumber = req.orderNumber)
        val saved = questionRepo.save(q)
        return AdminTestQuestionDto(saved.id!!, saved.text, saved.orderNumber, emptyList())
    }

    @PostMapping("/test-questions/update/{id}")
    fun updateQuestion(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateTestQuestionRequest
    ): AdminTestQuestionDto {
        val q = questionRepo.findById(id).orElseThrow { error("Question not found") }
        if (req.text != null) q.text = req.text
        if (req.orderNumber != null) q.orderNumber = req.orderNumber
        val saved = questionRepo.save(q)
        val answers = answerRepo.findAllByQuestionId(saved.id!!).map { a -> AdminTestAnswerDto(a.id!!, a.text, a.score) }
        return AdminTestQuestionDto(saved.id!!, saved.text, saved.orderNumber, answers)
    }

    @PostMapping("/test-questions/delete/{id}")
    fun deleteQuestion(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val q = questionRepo.findById(id).orElseThrow { error("Question not found") }
        questionRepo.delete(q)
    }

    // --- TEST ANSWER OPTIONS ---

    @PostMapping("/test-answers/add")
    fun createAnswer(@RequestBody req: CreateTestAnswerRequest): AdminTestAnswerDto {
        val q = questionRepo.findById(req.questionId).orElseThrow { error("Question not found") }
        val a = TestAnswerOption(question = q, text = req.text, score = req.score)
        val saved = answerRepo.save(a)
        return AdminTestAnswerDto(saved.id!!, saved.text, saved.score)
    }

    @PostMapping("/test-answers/update/{id}")
    fun updateAnswer(
        @org.springframework.web.bind.annotation.PathVariable id: Long,
        @RequestBody req: UpdateTestAnswerRequest
    ): AdminTestAnswerDto {
        val a = answerRepo.findById(id).orElseThrow { error("Answer not found") }
        if (req.text != null) a.text = req.text
        if (req.score != null) a.score = req.score
        val saved = answerRepo.save(a)
        return AdminTestAnswerDto(saved.id!!, saved.text, saved.score)
    }

    @PostMapping("/test-answers/delete/{id}")
    fun deleteAnswer(@org.springframework.web.bind.annotation.PathVariable id: Long) {
        val a = answerRepo.findById(id).orElseThrow { error("Answer not found") }
        answerRepo.delete(a)
    }
}
