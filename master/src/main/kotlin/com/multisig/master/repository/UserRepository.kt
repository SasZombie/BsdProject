package com.multisig.master.repository
import com.multisig.master.model.Role
import com.multisig.master.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean

    fun findAllByRole(role: Role): List<User>
}