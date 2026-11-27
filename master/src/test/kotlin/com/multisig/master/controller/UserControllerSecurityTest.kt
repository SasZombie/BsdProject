package com.multisig.master.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.multisig.master.config.SecurityConfig
import com.multisig.master.config.filters.TokenExchangeFilter
import com.multisig.master.dto.UserBackednResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.model.Role
import com.multisig.master.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
@Import(TokenExchangeFilter::class, SecurityConfig::class)
class UserControllerSecurityTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var redisTemplate: StringRedisTemplate

    @MockkBean
    private lateinit var valueOperations: ValueOperations<String, String>

    @BeforeEach
    fun setUp() {
        every { redisTemplate.opsForValue() } returns valueOperations
    }

    @Test
    fun `request without Authorization header returns 401 Unauthorized`() {
        mockMvc.perform(get("/api/v1/users/all"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `request with malformed Authorization header returns 401 Unauthorized`() {
        mockMvc.perform(
            get("/api/v1/users/alls")
                .header("Authorization", "Basic dXNlcjpwYXNz")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `request with expired or unknown token in Redis returns 401 Unauthorized`() {
        val opaqueToken = "invalid-or-expired-uuid"
        every { valueOperations.get("tokens:$opaqueToken") } returns null

        mockMvc.perform(
            get("/api/v1/users/all")
                .header("Authorization", "Bearer $opaqueToken")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `request with valid token populates user context and returns 200 OK`() {
        val validToken = "valid-session-uuid-123"

        val authUserJson = """
        {
            "id": 42,
            "role": "ADMIN"
        }
    """.trimIndent()

        every { valueOperations.get("tokens:$validToken") } returns authUserJson

        val mockUsers = listOf(
            UserBackednResponse(
                id = 42L,
                name = "Alice",
                surname = "Smith",
                username = "asmith",
                reportsTo = null,
                role = Role.ADMIN
            )
        )
        every { userService.getAllUsers() } returns mockUsers

        mockMvc.perform(
            get("/api/v1/users/all")
                .header("Authorization", "Bearer $validToken")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(42L))
            .andExpect(jsonPath("$[0].username").value("asmith"))
    }

    @Test
    fun `request with non-admin role returns 403 Forbidden`() {
        val validToken = "employee-token-456"
        val employeeJson = """
        {
            "id": 99,
            "role": "EMPLOYEE"
        }
    """.trimIndent()

        every { valueOperations.get("tokens:$validToken") } returns employeeJson

        mockMvc.perform(
            get("/api/v1/users/all")
                .header("Authorization", "Bearer $validToken")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }
}