package com.multisig.master.service

import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserResponse
import com.multisig.master.model.User
import com.multisig.master.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.Clock

@Service
class UserService(private val userRepository: UserRepository) {
    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            Exception("User with id $id not found")
        }
        return user.toResponse()
    }

    @Transactional
    fun createUser(request: UserCreateRequest): UserResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw Exception("Username ${request.username} already in use")
        }

        val newUser = User(
            name = request.name,
            surrname = request.surrname,
            username = request.username,
            passwordHash = hashPassword(request.password),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            role = request.role
        )

        return userRepository.save(newUser).toResponse()
    }

    private fun hashPassword(password: String): String = password

    private fun User.toResponse() = UserResponse(
        token = this.id.toString(),
        name = this.name,
        surname = this.surrname,
        username = this.username,
        role = this.role
    )
}