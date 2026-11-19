package com.multisig.master.service

import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.ExpenseCreateResponse
import com.multisig.master.dto.UserExpenseResponse
import com.multisig.master.dto.UserExpenseResponseWithDecision
import com.multisig.master.event.ExpenseApprovedEvent
import com.multisig.master.model.Expense
import com.multisig.master.model.ExpenseDecisions
import com.multisig.master.model.Role
import com.multisig.master.model.Status
import com.multisig.master.model.User
import com.multisig.master.repository.ExpensesDecisionRepository
import com.multisig.master.repository.ExpenseRepository
import com.multisig.master.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ExpensesService(
    private val requestsRepo: ExpenseRepository,
    private val reqDecisionRepo: ExpensesDecisionRepository,
    private val userRepo: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional(readOnly = true)
    fun getAllExpenses(id: String): List<UserExpenseResponse> {
        val array = requestsRepo.findAllByUserId(id.toLong())
        return array.toResponse()
    }

    @Transactional(readOnly = true)
    fun getExpensesToDecide(id: String): List<UserExpenseResponse> {
        val array = requestsRepo.findPendingExpensesByUserId(id.toLong())
        return array.toResponse()
    }

    @Transactional
    fun applyDecision(id: String, request: DecideExpense): UserExpenseResponseWithDecision {

        if (request.decision == Status.PENDING) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Cannot change status to PENDING"
            )
        }

        val decisionQuerry = reqDecisionRepo.findByUserIdAndExpenseId(id.toLong(), request.expenseId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Combination $id and ${request.expenseId} not found"
            )

        if (decisionQuerry.decision != Status.PENDING) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only pending decisions can be changed"
            )
        }
        decisionQuerry.decision = request.decision
        if (request.comment != null) {
            decisionQuerry.comment = request.comment
        }

        val initialReq = requestsRepo.findById(request.expenseId).orElseThrow {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "ExpenseId not found"
            )
        }
        //If not approved deny the whole request
        if (request.decision != Status.APPROVED) {

            initialReq.status = request.decision
        } else {
            //If approved, see if everyone approved
            val allWhoNeedToApprove = reqDecisionRepo.findAllByExpenseIdAndIdNot(request.expenseId, decisionQuerry.id!!)

            if (allWhoNeedToApprove.all { it.decision == Status.APPROVED }) {
                initialReq.status = Status.APPROVED

                eventPublisher.publishEvent(
                    ExpenseApprovedEvent(initialReq.id!!, initialReq.ammountDollars)
                )
            }
        }

        val returned = UserExpenseResponseWithDecision(
            id = initialReq.id!!,
            reason = initialReq.reason!!,
            createdAt = initialReq.createdAt!!,
            updatedAt = Instant.now(),
            status = initialReq.status,
            personalDecision = request.decision,
            ammountOfDollars = initialReq.ammountDollars
        )
        return returned
    }

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


    private fun List<Expense>.toResponse(): List<UserExpenseResponse> = this.map {
        UserExpenseResponse(
            id = it.id!!,
            reason = it.reason ?: "",
            createdAt = it.createdAt!!,
            updatedAt = it.updatedAt!!,
            status = it.status,
            ammountOfDollars = it.ammountDollars
        )
    }
}