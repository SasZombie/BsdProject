package com.multisig.master.service

import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.ExpenseCreateResponse
import com.multisig.master.model.Expense
import com.multisig.master.model.ExpenseDecisions
import com.multisig.master.model.Role
import com.multisig.master.model.Status
import com.multisig.master.model.User
import com.multisig.master.repository.RequestsDecisionRepository
import com.multisig.master.repository.RequestsRepository
import com.multisig.master.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class RequestService(
    private val requestsRepo: RequestsRepository,
    private val reqDecisionRepo: RequestsDecisionRepository,
    private val userRepo: UserRepository
) {

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
        val request = requestsRepo.save(newExpense).toResponse()
        decideApprovals(newExpense)
        return request
    }

    @Transactional
    private fun decideApprovals(expense: Expense) {

        val dollars = expense.ammountDollars
        val ceo = getUserByUsername("CEO")
        val cfo = getUserByUsername("CFO")
        val boardDir = getUserByUsername("BORDIR")

        val usersThatMustApprove = when (expense.user!!.role) {

            Role.CEO -> {
                listOf(cfo, boardDir)

            }

            Role.CFO -> {
                listOf(ceo, boardDir)

            }

            Role.BOARD_DIRECTOR -> {

                listOf(ceo, cfo)
            }

            else -> {

                when {
                    dollars > 100_000_00 -> {
                        listOf(
                            ceo, cfo
                        )
                    }

                    dollars > 10_000_00 -> {

                        val manager1 = expense.user?.reportsTo ?: ceo
                        val manager2 = expense.user?.reportsTo?.reportsTo ?: boardDir

                        listOf(cfo, manager1, manager2).distinct()
                    }

                    dollars > 5_000_00 -> {

                        val manager1 = expense.user?.reportsTo ?: ceo
                        val manager2 = expense.user?.reportsTo?.reportsTo ?: cfo

                        listOf(manager1, manager2)
                    }

                    else -> {
                        println("AIci se executa!")
                        val manager1 = expense.user?.reportsTo ?: cfo
                        println(expense.user?.surname)
                        println(expense.user?.reportsTo?.surname)

                        listOf(manager1)
                    }
                }
            }
        }

        for (userThatMustApprove in usersThatMustApprove) {
            val newDecision = ExpenseDecisions(
                user = userThatMustApprove,
                expense = expense
            )
            reqDecisionRepo.save(newDecision)
        }

    }


    private fun getUserByUsername(username: String): User =
        userRepo.findByUsername(username)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "$username not found") }

    // ExpenseCreateResponse
//TODO: Update this to reflect approvals neededs
    @Suppress("UnusedReceiverParameter")
    private fun Expense.toResponse() = ExpenseCreateResponse(
        message = "Created!",
        approvalsNeededCount = 2
    )
}