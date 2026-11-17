package com.multisig.master.service

import com.multisig.master.dto.UserBackednResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.dto.UserUpdateRequest
import com.multisig.master.model.User
import com.multisig.master.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import kotlin.jvm.optionals.getOrElse

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
            val reportsToNew = userRepository.findByUsername(newFields.reportsTo)
                .orElseThrow { NoSuchElementException("User to report not found with username: ${newFields.reportsTo}") }

            user.reportsTo = reportsToNew
        } else {
            user.reportsTo = null
        }

        return user.toBackendResponse()
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserBackednResponse> {
        return userRepository.findAll().toResponse()
    }


    @Transactional
    fun createUser(request: UserCreateRequest): UserBackednResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username ${request.username} already in use")
        }

        val manager = request.reportsTo?.let { username ->
            userRepository.findByUsername(username)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Reports to user '$username' not found") }
        }

        val newUser = User(
            name = request.name,
            surname = request.surname,
            username = request.username,
            passwordHash = hashPassword(request.password),
            reportsTo = manager,
            role = request.role
        )

        return userRepository.save(newUser).toBackendResponse()
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