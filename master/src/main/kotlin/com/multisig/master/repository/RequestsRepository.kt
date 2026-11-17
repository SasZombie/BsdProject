package com.multisig.master.repository
import com.multisig.master.model.Expense
import com.multisig.master.model.Role
import com.multisig.master.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RequestsRepository : JpaRepository<Expense, Long> 
{
 
    fun findAllByUserId(userId: Long): List<Expense>
}