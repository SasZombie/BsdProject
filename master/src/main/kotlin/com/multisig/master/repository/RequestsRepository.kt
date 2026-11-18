package com.multisig.master.repository

import com.multisig.master.model.Expense
import com.multisig.master.model.Role
import com.multisig.master.model.Status
import com.multisig.master.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RequestsRepository : JpaRepository<Expense, Long> {
    fun findAllByUserId(userId: Long): List<Expense>

    @Query("""
        SELECT ed.expense 
        FROM ExpenseDecisions ed 
        WHERE ed.user.id = :userId 
          AND ed.decision = :status
        """)
    fun findPendingExpensesByUserId(@Param("userId") userId: Long, @Param("status") status: Status = Status.PENDING): List<Expense>
}