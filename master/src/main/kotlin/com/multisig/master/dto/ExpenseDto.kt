package com.multisig.master.dto

import com.multisig.master.model.Status
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class ExpenseCreateRequest(
    
    @field:NotNull(message = "Cannot have blank Id")
    val userId: Long,
    
    @field:NotNull(message = "Ammount cannot be blank")
    @field:Min(value = 1 , message = "Ammount cant be 0")
    val ammountDollars: Long,

    @field:Size(max = 500, message = "Reason cannot exceed 500 characters")
    val reason: String
)

data class ExpensesGetRequest(
    @field:NotNull(message = "Cannot have blank Id")
    val userId: Long
)


data class DecideExpense(
    val expenseId: Long,
    val decision: Status,
    val comment: String?
)

data class UserExpenseResponse(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val ammountOfDollars: Long,
    val needsApprovalBy: List<String>,
    val approvedBy: List<String>,
    val rejectedBy: List<String>,
    
)

data class UserExpenseResponseExtened(
    val expense: UserExpenseResponse,
    val user: UserBackednResponse
)

data class UserExpenseResponseWithDecision(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val personalStatus: Status,
    val ammountOfDollars: Long,
    val userName: String
)