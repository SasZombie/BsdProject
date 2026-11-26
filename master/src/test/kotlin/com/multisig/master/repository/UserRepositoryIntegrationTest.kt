package com.multisig.master.repository

import com.multisig.master.model.Role
import com.multisig.master.model.User
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.data.redis.core.StringRedisTemplate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Autowired
    private lateinit var userRepository: UserRepository
    
    @MockkBean(relaxed = true)
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Test
    fun `persisting and querying user preserves native PostgreSQL role enum`() {
        val manager = User(
            name = "Director",
            surname = "Boss",
            username = "dboss",
            passwordHash = "hash123",
            role = Role.BOARD_DIRECTOR
        )
        val savedManager = userRepository.save(manager)

        val employee = User(
            name = "Dev",
            surname = "Worker",
            username = "dworker",
            passwordHash = "hash456",
            role = Role.EMPLOYEE,
            reportsTo = savedManager
        )
        val savedEmployee = userRepository.save(employee)

        val retrieved = userRepository.findByUsername("dworker")
        assertTrue(retrieved.isPresent)
        assertEquals(Role.EMPLOYEE, retrieved.get().role)
        assertEquals("dboss", retrieved.get().reportsTo?.username)
        assertEquals(Role.BOARD_DIRECTOR, retrieved.get().reportsTo?.role)
    }

    @Test
    fun `existsByUsername correctly returns boolean flags`() {
        val user = User(
            name = "Test",
            surname = "User",
            username = "unique_login",
            passwordHash = "hash",
            role = Role.ACCOUNTANT
        )
        userRepository.save(user)

        assertTrue(userRepository.existsByUsername("unique_login"))
        assertFalse(userRepository.existsByUsername("non_existent_login"))
    }
}