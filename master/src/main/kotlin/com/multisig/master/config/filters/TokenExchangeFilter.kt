package com.multisig.master.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenExchangeFilter(
    private val redisTemplate: StringRedisTemplate
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/api/v1/auth")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing Authorization header")
            return
        }

        val opaque = authHeader.removePrefix("Bearer ").trim()

        val redisKey = "tokens$opaque"
        val realUserId = redisTemplate.opsForValue().get(redisKey)

        if (realUserId == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired session token")
            return
        }

        request.setAttribute("CurrentUserId", realUserId)

        filterChain.doFilter(request, response)
    }
}