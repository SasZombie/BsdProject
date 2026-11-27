package com.multisig.master.service

import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.UserBackednResponse
import com.multisig.master.dto.UserExpenseResponse
import com.multisig.master.dto.UserExpenseResponseExtened
import com.multisig.master.dto.UserExpenseResponseWithDecision
import com.multisig.master.dto.VetoExpense
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
    fun getAllPersonalExpenses(id: Long): List<UserExpenseResponse> {
        val requests = requestsRepo.findAllByUserId(id)
        if (requests.isEmpty()) return emptyList()

        val requestIds = requests.mapNotNull { it.id }
        //Map<ExpenseID -> ListOfExpenseDecisions>
        val decisionsByRequestId = reqDecisionRepo.findAllByExpenseIdIn(requestIds)
            .groupBy { it.expense!!.id }

        return requests.map { req ->
            val decisions = decisionsByRequestId[req.id].orEmpty()
            val (pendingDecisions, decidedDecisions) = decisions.partition { it.decision == Status.PENDING }
            val (approvedBy, rejectedBy) = decidedDecisions.partition { it.decision == Status.APPROVED }
                .let { (approved, denied) ->
                    approved.map { it.user!!.name } to denied.map { it.user!!.name }
                }

            val pending = pendingDecisions.map { it.user!!.name }
            req.toResponse(pending, approvedBy, rejectedBy = rejectedBy)
        }
    }

    @Transactional
    fun veto(userId: Long, request: VetoExpense) {
        println(
            "This veto function is deeply integrated with the payment system" +
                    "that is not implemented, so I will also skip the boilerplate"
        )

        val expense =
            requestsRepo.findById(request.expenseId).orElseThrow { NoSuchElementException("This request doesnt exist") }

        expense.status = Status.VETOED

    }

    @Transactional(readOnly = true)
    fun getAllExpenses(): List<UserExpenseResponseExtened> {
        val requests = requestsRepo.findAll()
        if (requests.isEmpty()) return emptyList()

        val requestIds = requests.mapNotNull { it.id }
        //Map<ExpenseID -> ListOfExpenseDecisions>
        val decisionsByRequestId = reqDecisionRepo.findAllByExpenseIdIn(requestIds)
            .groupBy { it.expense!!.id }

        return requests.map { req ->
            val decisions = decisionsByRequestId[req.id].orEmpty()
            val (pendingDecisions, decidedDecisions) = decisions.partition { it.decision == Status.PENDING }
            val (approvedBy, rejectedBy) = decidedDecisions.partition { it.decision == Status.APPROVED }
                .let { (approved, denied) ->
                    approved.map { it.user!!.name } to denied.map { it.user!!.name }
                }

            val pending = pendingDecisions.map { it.user!!.name }
            req.toResponseExtened(pending, approvedBy, rejectedBy = rejectedBy)
        }

    }

    @Transactional(readOnly = true)
    fun getExpensesDecisions(userId: Long): List<UserExpenseResponseWithDecision> {

        val expenses = requestsRepo.findExpensesByUserId(userId)
        if (expenses.isEmpty()) return emptyList()

        val expenseIds = expenses.mapNotNull { it.id }
        val decisionsByExpenseId: Map<Long, ExpenseDecisions> = reqDecisionRepo
            .findAllByUserIdAndExpenseIdIn(userId, expenseIds)
            .associateBy { it.expense!!.id!! }

        return expenses.map { expense ->
            val decision = decisionsByExpenseId[expense.id]?.decision ?: Status.PENDING

            UserExpenseResponseWithDecision(
                id = expense.id!!,
                reason = expense.reason.orEmpty(),
                createdAt = expense.createdAt!!,
                updatedAt = expense.updatedAt!!,
                status = expense.status,
                personalStatus = decision,
                ammountOfDollars = expense.ammountDollars,
                userName = expense.user?.name.orEmpty()
            )
        }
    }

    @Transactional
    fun applyDecision(id: Long, request: DecideExpense): UserExpenseResponseWithDecision {

        if (request.decision == Status.PENDING) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Cannot change status to PENDING"
            )
        }

        val decisionQuerry = reqDecisionRepo.findByUserIdAndExpenseId(id, request.expenseId)
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
            personalStatus = request.decision,
            ammountOfDollars = initialReq.ammountDollars,
            userName = initialReq.user!!.name
        )
        return returned
    }

    @Transactional
    fun create(userId: Long, request: ExpenseCreateRequest): UserExpenseResponse {

        val userProxy = userRepo.getReferenceById(userId)

        val newExpense = Expense(
            user = userProxy,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            ammountDollars = request.ammountDollars,
            reason = request.reason,
            status = Status.PENDING
        )
        val request = requestsRepo.save(newExpense)
        val usersThatMustApprove = decideApprovals(newExpense)
        val response = UserExpenseResponse(
            id = request.id!!,
            reason = request.reason!!,
            createdAt = request.createdAt!!,
            updatedAt = request.updatedAt!!,
            status = request.status,
            ammountOfDollars = request.ammountDollars,
            needsApprovalBy = usersThatMustApprove.map { user -> user.name },
            approvedBy = emptyList(),
            rejectedBy = emptyList()
        )

        return response
    }

    @Transactional
    private fun decideApprovals(expense: Expense): List<User> {

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
                        val manager1 = expense.user?.reportsTo ?: cfo
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

        return usersThatMustApprove
    }


    private fun getUserByUsername(username: String): User =
        userRepo.findByUsername(username)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "$username not found") }


    private fun Expense.toResponse(
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): UserExpenseResponse =
        UserExpenseResponse(
            id = this.id!!,
            reason = this.reason ?: "",
            createdAt = this.createdAt!!,
            updatedAt = this.updatedAt!!,
            status = this.status,
            ammountOfDollars = this.ammountDollars,
            needsApprovalBy = needsApprovalBy,
            approvedBy = approvedBy,
            rejectedBy = rejectedBy
        )

    private fun Expense.toResponseExtened(
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): UserExpenseResponseExtened =
        UserExpenseResponseExtened(
            UserExpenseResponse(
                id = id!!,
                reason = reason ?: "",
                createdAt = createdAt!!,
                updatedAt = updatedAt!!,
                status = status,
                ammountOfDollars = ammountDollars,
                needsApprovalBy = needsApprovalBy,
                approvedBy = approvedBy,
                rejectedBy = rejectedBy
            ),
            UserBackednResponse(
                id = user!!.id!!,
                name = user!!.name,
                surname = user!!.surname,
                username = user!!.username,
                reportsTo = user!!.reportsTo?.name ?: "None",
                role = user!!.role,
            )
        )

    private fun List<Expense>.toResponse(
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): List<UserExpenseResponse> = this.map {
        it.toResponse(needsApprovalBy, approvedBy, rejectedBy)
    }

    private fun List<Expense>.toResponseExtened(
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): List<UserExpenseResponseExtened> = this.map {
        it.toResponseExtened(needsApprovalBy, approvedBy, rejectedBy)
    }
}