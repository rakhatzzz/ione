package com.diploma.ione.exception

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalStateException(ex: IllegalStateException): Map<String, Any?> {
        return mapOf(
            "message" to (ex.message ?: "Bad request")
        )
    }

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleBadCredentialsException(ex: BadCredentialsException): Map<String, Any?> {
        return mapOf(
            "message" to "Неверный логин или пароль"
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): Map<String, Any?> {
        val errors =
            ex.bindingResult
                .allErrors
                .mapNotNull { err ->
                    val field = (err as? FieldError)?.field
                    val message = err.defaultMessage
                    if (field == null) return@mapNotNull null
                    field to (message ?: "Invalid value")
                }
                .toMap()

        return mapOf(
            "message" to "Validation failed",
            "errors" to errors
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): Map<String, Any?> {
        return mapOf(
            "message" to (ex.message ?: "Internal server error")
        )
    }
}
