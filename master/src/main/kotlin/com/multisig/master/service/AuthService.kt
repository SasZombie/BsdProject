package com.multisig.master.service

import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    fun login(request: UserLogInRequest): UserLogInResponse {
        
        print(request)
        val user = userRepository.findByUsername(request.username)
            .orElseThrow {
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
            }

        if (user.passwordHash != request.password) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password")
        }

        return UserLogInResponse(
            //TODO: This needs redis proxy
            token = user.id.toString(),
            role = user.role
        )
    }
}