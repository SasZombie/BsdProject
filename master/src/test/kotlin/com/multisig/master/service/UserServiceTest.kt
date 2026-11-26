package com.multisig.master.service

import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserUpdateRequest
import com.multisig.master.model.Role
import com.multisig.master.model.User
import com.multisig.master.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.util.*

class UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<Argon2PasswordEncoder>()

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, passwordEncoder)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createUser successfully creates a user and hashes password`() {

        val request = UserCreateRequest(
            name = "John",
            surname = "Doe",
            username = "johndoe",
            password = "rawPassword123",
            reportsTo = null,
            role = Role.EMPLOYEE
        )

        every { userRepository.existsByUsername("johndoe") } returns false
        every { passwordEncoder.encode("rawPassword123") } returns "hashedArgon2String"

        every { userRepository.save(any()) } answers {
            val userArg = firstArg<User>()
            userArg.id = 10L
            userArg
        }

        val result = userService.createUser(request)

        assertEquals(10L, result.id)
        assertEquals("johndoe", result.username)
        assertEquals(Role.EMPLOYEE, result.role)
        assertNull(result.reportsTo)

        verify(exactly = 1) { passwordEncoder.encode("rawPassword123") }
        verify(exactly = 1) { userRepository.save(match { it.passwordHash == "hashedArgon2String" }) }
    }

    @Test
    fun `createUser throws 409 Conflict when username already exists`() {

        val request = UserCreateRequest(
            name = "Jane",
            surname = "Doe",
            username = "existing_user",
            password = "pwd",
            reportsTo = null,
            role = Role.EMPLOYEE
        )

        every { userRepository.existsByUsername("existing_user") } returns true

        val exception = assertThrows<ResponseStatusException> {
            userService.createUser(request)
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        assertTrue(exception.reason!!.contains("already in use"))

        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `createUser throws 404 Not Found when reportsTo manager does not exist`() {

        val request = UserCreateRequest(
            name = "Bob",
            surname = "Smith",
            username = "bobsmith",
            password = "pwd",
            reportsTo = "non_existent_manager",
            role = Role.EMPLOYEE
        )

        every { userRepository.existsByUsername("bobsmith") } returns false
        every { userRepository.findByUsername("non_existent_manager") } returns Optional.empty()

        val exception = assertThrows<ResponseStatusException> {
            userService.createUser(request)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertTrue(exception.reason!!.contains("Reports to user 'non_existent_manager' not found"))
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `updateUser modifies role and clears reportsTo when null`() {

        val existingUser = User(
            name = "Alice",
            surname = "Smith",
            username = "alicesmith",
            passwordHash = "someHash",
            role = Role.EMPLOYEE,
            reportsTo = User(name = "Boss", surname = "Man", username = "boss", passwordHash = "h", role = Role.MANAGER)
        ).apply { id = 5L }

        val updateRequest = UserUpdateRequest(
            role = Role.ADMIN,
            reportsTo = null
        )

        every { userRepository.findById(5L) } returns Optional.of(existingUser)

        val response = userService.updateUser(5L, updateRequest)

        assertEquals(Role.ADMIN, response.role)
        assertNull(response.reportsTo)
        assertNull(existingUser.reportsTo)
    }


    @Test
    fun `getUserById returns response when user exists`() {
        val user = User(
            name = "Charlie",
            surname = "Brown",
            username = "cbrown",
            passwordHash = "hash",
            role = Role.EMPLOYEE
        ).apply { id = 42L }

        every { userRepository.findById(42L) } returns Optional.of(user)

        val result = userService.getUserById(42L)

        assertEquals("42", result.token)
        assertEquals("Charlie", result.name)
        assertEquals(Role.EMPLOYEE, result.role)
    }

    @Test
    fun `getUserById throws Exception when user not found`() {
        every { userRepository.findById(999L) } returns Optional.empty()

        val exception = assertThrows<Exception> {
            userService.getUserById(999L)
        }

        assertEquals("User with id 999 not found", exception.message)
    }
}