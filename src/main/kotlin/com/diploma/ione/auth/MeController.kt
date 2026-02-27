package com.diploma.ione.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/me")
class MeController {
    @GetMapping
    fun me(): Map<String, Any?> {
        val auth = SecurityContextHolder.getContext().authentication
        return mapOf(
            "userId" to auth?.principal,
            "authorities" to auth?.authorities?.map { it.authority }
        )
    }
}