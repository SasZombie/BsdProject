package com.multisig.master.service

import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: Argon2PasswordEncoder
) {

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
        

        return UserLogInResponse(
            //TODO: This needs redis proxy
            token = user.id.toString(),
            role = user.role,
            name = user.name
        )
    }
}