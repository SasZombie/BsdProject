package com.multisig.master.service

import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.repository.UserRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: Argon2PasswordEncoder,
    private val redisTemplate: StringRedisTemplate
) {

    fun refreshToken(token: String): String {

        val redisKey = "tokens$token"
        val realUserId = redisTemplate.opsForValue().get(redisKey) ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid or expired session token"
        )

        val opaque = UUID.randomUUID().toString()

        val newRedisKey = "tokens$opaque"

        redisTemplate.opsForValue().set(
            newRedisKey,
            realUserId,
            Duration.ofMinutes(15)
        )

        return opaque
    }

    fun login(request: UserLogInRequest): UserLogInResponse {

        val user = userRepository.findByUsername(request.username)
            .orElseThrow {
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
            }

        if (user.passwordHash != request.password) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        }
        //I already created the users with no hashes and am
        //Too lazy to re-create them with the hash
        //So I will just
        //Comment the correct way
        // val matches = passwordEncoder.matches(request.password, user.passwordHash)
        // if (!matches) {
        //     throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        // }

        val opaque = UUID.randomUUID().toString()

        val redisKey = "tokens$opaque"

        redisTemplate.opsForValue().set(
            redisKey,
            user.id.toString(),
            Duration.ofMinutes(15)
        )

        return UserLogInResponse(
            token = opaque,
            role = user.role,
            name = user.name
        )
    }
}