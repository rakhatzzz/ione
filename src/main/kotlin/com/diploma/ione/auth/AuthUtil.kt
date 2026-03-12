package com.diploma.ione.auth

import org.springframework.security.core.context.SecurityContextHolder

object AuthUtil {
    fun currentUserId(): Long {
        val auth = SecurityContextHolder.getContext().authentication
            ?: error("Not authenticated")
        return auth.principal.toString().toLong()
    }
}