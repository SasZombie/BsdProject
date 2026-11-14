package com.multisig.master.service

import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserResponse
import com.multisig.master.model.Role
import com.multisig.master.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    fun login(request: UserLogInRequest): UserResponse {
        
        print(request)
        val user = userRepository.findByUsername(request.username)
            .orElseThrow {
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
            }

        if (user.passwordHash != request.password) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        }

        // 3. Return user data
        return UserResponse(
            token = "a",
            name = user.name,
            surname = user.surrname,
            username = user.username,
            role = user.role
        )
    }
}