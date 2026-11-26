package com.multisig.master.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.multisig.master.dto.UserBackednResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.model.Role
import com.multisig.master.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerSecurityTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean(relaxed = true)
    private lateinit var stringRedisTemplate: StringRedisTemplate
    
    @Test
    fun `unauthenticated requests to protected endpoints return 401 or 403`() {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "regular_user", roles = ["ADMIN"])
    fun `authenticated user can list users`() {
        val mockUsers = listOf(
            UserBackednResponse(
                id = 1L,
                name = "Alice",
                surname = "Smith",
                username = "asmith",
                reportsTo = null,
                role = Role.EMPLOYEE
            )
        )
        every { userService.getAllUsers() } returns mockUsers

        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].username").value("asmith"))
            .andExpect(jsonPath("$[0].role").value("EMPLOYEE"))
    }

    @Test
    @WithMockUser(username = "admin_user", roles = ["ADMIN"])
    fun `admin user can create a new user`() {
        val request = UserCreateRequest(
            name = "Bob",
            surname = "Jones",
            username = "bjones",
            password = "SecretPassword123",
            reportsTo = null,
            role = Role.EMPLOYEE
        )

        val response = UserBackednResponse(
            id = 2L,
            name = "Bob",
            surname = "Jones",
            username = "bjones",
            reportsTo = null,
            role = Role.EMPLOYEE
        )

        every { userService.createUser(any()) } returns response

        mockMvc.perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2L))
            .andExpect(jsonPath("$.username").value("bjones"))
    }

    @Test
    @WithMockUser(username = "employee_user", roles = ["EMPLOYEE"])
    fun `standard employee cannot access admin endpoints`() {
        val request = UserCreateRequest(
            name = "Hacker",
            surname = "User",
            username = "hacker",
            password = "pwd",
            reportsTo = null,
            role = Role.ADMIN
        )

        mockMvc.perform(
            post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }
}