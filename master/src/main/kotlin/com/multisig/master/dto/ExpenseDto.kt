package com.multisig.master.dto

import com.multisig.master.model.Role
import com.multisig.master.model.Status
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigInteger
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


data class ExpenseCreateResponse(
    val message: String,
    val approvalsNeededCount: Int
)

//Do I want to delete a request?? Maybe
data class UserExpenseResponse(
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val ammountOfDollars: Long
)

data class ExpnseResponse(
    val status: Status,
    val comments: Array<String>,
    val userExpensesReposes: Array<UserExpenseResponse> 
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExpnseResponse

        if (status != other.status) return false
        if (!comments.contentEquals(other.comments)) return false
        if (!userExpensesReposes.contentEquals(other.userExpensesReposes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + comments.contentHashCode()
        result = 31 * result + userExpensesReposes.contentHashCode()
        return result
    }
}