package com.multisig.master.service

import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.ExpenseCreateResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserResponse
import com.multisig.master.model.Expense
import com.multisig.master.model.Status
import com.multisig.master.model.User
import com.multisig.master.repository.RequestsRepository
import com.multisig.master.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.Clock

@Service
class RequestService(private val requestsRepo: RequestsRepository, private val userRepo: UserRepository) {


    @Transactional
    fun create(request: ExpenseCreateRequest): ExpenseCreateResponse {

        val userProxy = userRepo.getReferenceById(request.userId)

        val newExpense = Expense(
            user = userProxy,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            ammountDollars = request.ammountDollars,
            reason = request.reason,
            status = Status.PENDING
        )
        return requestsRepo.save(newExpense).toResponse()
    }
// @Transactional
// fun createUser(request: UserCreateRequest): UserResponse {
//     if (userRepository.existsByUsername(request.username)) {
//         throw Exception("Username ${request.username} already in use")
//     }

//     val newUser = User(
//         name = request.name,
//         surrname = request.surrname,
//         username = request.username,
//         passwordHash = hashPassword(request.password),
//         createdAt = Instant.now(),
//         updatedAt = Instant.now(),
//         role = request.role
//     )

//     return userRepository.save(newUser).toResponse()
// }

// private fun hashPassword(password: String): String = password

    // private fun User.toResponse() = (
    //     token = this.id.toString(),
    //     name = this.name,
    //     surname = this.surrname,
    //     username = this.username,
    //     role = this.role
    // )

    // ExpenseCreateResponse
    //TODO: Update this to reflect approvals neededs
    @Suppress("UnusedReceiverParameter")
    private fun Expense.toResponse() = ExpenseCreateResponse(
        message = "Created!",
        approvalsNeededCount = 2
    )
}