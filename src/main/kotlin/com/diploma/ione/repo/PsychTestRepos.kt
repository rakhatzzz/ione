package com.diploma.ione.repo

import com.diploma.ione.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PsychologicalTestRepo : JpaRepository<PsychologicalTest, Long>

interface TestCategoryRepo : JpaRepository<TestCategory, Long> {
    fun findAllByTestId(testId: Long): List<TestCategory>
}

interface CategoryZoneRepo : JpaRepository<CategoryZone, Long> {
    fun findAllByCategoryId(categoryId: Long): List<CategoryZone>
    fun findAllByCategoryIdIn(categoryIds: List<Long>): List<CategoryZone>
}

interface TestQuestionRepo : JpaRepository<TestQuestion, Long> {
    fun findAllByTestIdOrderByOrderNumberAsc(testId: Long): List<TestQuestion>
}

interface TestAnswerOptionRepo : JpaRepository<TestAnswerOption, Long> {
    fun findAllByQuestionId(questionId: Long): List<TestAnswerOption>
}

interface TestAttemptRepo : JpaRepository<TestAttempt, Long> {
    fun findAllByStudentIdAndTestIdOrderByStartedAtDesc(studentId: Long, testId: Long): List<TestAttempt>

    fun findAllByStudentIdOrderByStartedAtDesc(studentId: Long): List<TestAttempt>

    @Query("""
        select a from TestAttempt a
        join fetch a.test t
        where a.student.id = :studentId
        order by a.startedAt desc
    """)
    fun findAllByStudentIdWithTestOrderByStartedAtDesc(@Param("studentId") studentId: Long): List<TestAttempt>

    @Query("""
        select a from TestAttempt a
        join fetch a.student s
        join fetch s.user su
        join fetch a.test t
        where a.id = :attemptId
    """)
    fun findByIdWithStudentAndTest(@Param("attemptId") attemptId: Long): TestAttempt?
}

interface TestAttemptAnswerRepo : JpaRepository<TestAttemptAnswer, Long> {
    fun findAllByAttemptId(attemptId: Long): List<TestAttemptAnswer>
    fun findByAttemptIdAndQuestionId(attemptId: Long, questionId: Long): TestAttemptAnswer?

    @Query("""
        select a from TestAttemptAnswer a
        join fetch a.question q
        join fetch q.category c
        join fetch a.selectedOption o
        where a.attempt.id = :attemptId
        order by q.orderNumber asc
    """)
    fun findAllByAttemptIdDetailed(@Param("attemptId") attemptId: Long): List<TestAttemptAnswer>

    @Query("""
        select q.category.id as categoryId, sum(a.score) as totalScore
        from TestAttemptAnswer a
        join a.question q
        where a.attempt.id = :attemptId
        group by q.category.id
    """)
    fun sumScoresByCategory(@Param("attemptId") attemptId: Long): List<Array<Any>>
}