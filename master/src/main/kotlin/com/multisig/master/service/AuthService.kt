package com.multisig.master.service

import com.multisig.master.dto.AuthUser
import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.repository.UserRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: Argon2PasswordEncoder,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    fun refreshToken(token: String): String {

        val redisKey = "tokens:$token"

        val userJson = redisTemplate.opsForValue().get(redisKey) ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid or expired session token"
        )

        val authUser = objectMapper.readValue(userJson, AuthUser::class.java)

        val opaque = UUID.randomUUID().toString()

        val userValue = AuthUser(
            authUser.id,
            authUser.role
        )
        val newRedisKey = "tokens:$opaque"

        redisTemplate.opsForValue().set(newRedisKey, userJson, Duration.ofMinutes(15))


        return opaque
    }

    fun endToken(token: String) {
        redisTemplate.delete(token)
    }

    fun login(request: UserLogInRequest): UserLogInResponse {

        val user = userRepository.findByUsername(request.username)
            .orElseThrow {
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
            }

        val matches = passwordEncoder.matches(request.password, user.passwordHash)
        if (!matches) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        }

        val opaque = UUID.randomUUID().toString()

        val userValue = AuthUser(
            user.id!!,
            user.role
        )
        val redisKey = "tokens:$opaque"

        val userJson = objectMapper.writeValueAsString(userValue)
        redisTemplate.opsForValue().set(redisKey, userJson, Duration.ofMinutes(15))


        return UserLogInResponse(
            token = opaque,
            role = user.role,
            name = user.name
        )
    }
}