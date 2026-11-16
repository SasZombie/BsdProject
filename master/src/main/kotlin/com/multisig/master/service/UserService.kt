package com.multisig.master.service

import com.multisig.master.dto.UserBackednResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.dto.UserUpdateRequest
import com.multisig.master.model.User
import com.multisig.master.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserService(private val userRepository: UserRepository) {
    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserLogInResponse {
        val user = userRepository.findById(id).orElseThrow {
            Exception("User with id $id not found")
        }
        return user.toResponse()
    }

    @Transactional
    fun updateUser(userToBeUpdated: Long, newFields: UserUpdateRequest): UserBackednResponse {
        println("New Fields")
        println(newFields)
        println("USERRRRRRRRR to be Updated: $userToBeUpdated")
        println(userToBeUpdated)

        val user = userRepository.findById(userToBeUpdated)
            .orElseThrow { NoSuchElementException("User not found with id: $userToBeUpdated") }

        newFields.role?.let { user.role = it }     
        if (newFields.reportsTo != null) {
            println("HEre")
            println("HEre")
            println("HEre")
            println("HEre")
            val reportsToNew = userRepository.findByUsername(newFields.reportsTo)
                .orElseThrow { NoSuchElementException("User to report not found with username: ${newFields.reportsTo}") }
            
            user.reportsTo = reportsToNew
        }

        return user.toBackendResponse()
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserBackednResponse> {
        return userRepository.findAll().toResponse()
    }


    @Transactional
    fun createUser(request: UserCreateRequest): UserLogInResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw Exception("Username ${request.username} already in use")
        }

        val newUser = User(
            name = request.name,
            surname = request.surname,
            username = request.username,
            passwordHash = hashPassword(request.password),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            role = request.role
        )

        return userRepository.save(newUser).toResponse()
    }

    private fun hashPassword(password: String): String = password

    private fun User.toResponse() = UserLogInResponse(
        token = this.id.toString(),
        role = this.role
    )

    private fun User.toBackendResponse() = UserBackednResponse(
        id = this.id!!,
        name = this.name,
        surname = this.surname,
        username = this.username,
        reportsTo = this.reportsTo?.username,
        role = this.role,
    )


    private fun List<User>.toResponse(): List<UserBackednResponse> = this.map { it.toBackendResponse() }
}