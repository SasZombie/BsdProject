package com.multisig.master.config.filters

import com.multisig.master.dto.AuthUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

@Component
class TokenExchangeFilter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
    
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/api/v1/auth") || uri.startsWith("/actuator") || 
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/swagger-ui") ||
               uri == "/swagger-ui.html"
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

        val redisKey = "tokens:$opaque"
        val userJson = redisTemplate.opsForValue().get(redisKey)

        if (userJson == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired session token")
            return
        }

        val authUser = objectMapper.readValue(userJson, AuthUser::class.java)

        //Spring Security
        val authorities = listOf(SimpleGrantedAuthority(authUser.role.name))
        val authToken = UsernamePasswordAuthenticationToken(authUser, null, authorities)
        SecurityContextHolder.getContext().authentication = authToken

        request.setAttribute("CurrentUser", authUser)

        filterChain.doFilter(request, response)
    }
}