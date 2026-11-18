package com.multisig.master.repository

import com.multisig.master.model.Expense
import com.multisig.master.model.ExpenseDecisions
import com.multisig.master.model.Role
import com.multisig.master.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RequestsDecisionRepository : JpaRepository<ExpenseDecisions, Long> {

    fun findByUserIdAndExpenseId(userId: Long, expenseId: Long): ExpenseDecisions?
    
    fun findAllByExpenseId(expenseId: Long): List<ExpenseDecisions>
    
    fun findAllByExpenseIdAndIdNot(expenseId: Long, expenseDecisionIdToExclude: Long): List<ExpenseDecisions>    
    
        
}