package com.multisig.master.repository

import com.multisig.master.model.ExpenseDecisions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExpensesDecisionRepository : JpaRepository<ExpenseDecisions, Long> {

    fun findByUserIdAndExpenseId(userId: Long, expenseId: Long): ExpenseDecisions?
    
    fun findAllByExpenseId(expenseId: Long): List<ExpenseDecisions>
    
    fun findAllByExpenseIdAndIdNot(expenseId: Long, expenseDecisionIdToExclude: Long): List<ExpenseDecisions>            
}