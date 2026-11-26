package com.multisig.master.service

import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.VetoExpense
import com.multisig.master.event.ExpenseApprovedEvent
import com.multisig.master.model.Expense
import com.multisig.master.model.ExpenseDecisions
import com.multisig.master.model.Role
import com.multisig.master.model.Status
import com.multisig.master.model.User
import com.multisig.master.repository.ExpenseRepository
import com.multisig.master.repository.ExpensesDecisionRepository
import com.multisig.master.repository.UserRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.Optional

class ExpensesServiceTest {

    private val requestsRepo = mockk<ExpenseRepository>()
    private val reqDecisionRepo = mockk<ExpensesDecisionRepository>()
    private val userRepo = mockk<UserRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private lateinit var expensesService: ExpensesService

    @BeforeEach
    fun setUp() {
        expensesService = ExpensesService(
            requestsRepo,
            reqDecisionRepo,
            userRepo,
            eventPublisher
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `applyDecision throws Forbidden when decision is PENDING`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.PENDING, comment = null)

        val ex = assertThrows<ResponseStatusException> {
            expensesService.applyDecision(10L, request)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertTrue(ex.reason!!.contains("Cannot change status to PENDING"))
        verify(exactly = 0) { reqDecisionRepo.findByUserIdAndExpenseId(any(), any()) }
    }

    @Test
    fun `applyDecision throws NotFound when user decision combination does not exist`() {
        val request = DecideExpense(expenseId = 2L, decision = Status.APPROVED, comment = null)
        every { reqDecisionRepo.findByUserIdAndExpenseId(10L, 2L) } returns null

        val ex = assertThrows<ResponseStatusException> {
            expensesService.applyDecision(10L, request)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun `applyDecision throws Forbidden when decision was already resolved`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.APPROVED, comment = null)
        val existingDecision = ExpenseDecisions(
            id = 5L,
            user = User(name = "Manager", surname = "M", username = "mgr", passwordHash = "h", role = Role.MANAGER),
            decision = Status.APPROVED
        )

        every { reqDecisionRepo.findByUserIdAndExpenseId(10L, 1L) } returns existingDecision

        val ex = assertThrows<ResponseStatusException> {
            expensesService.applyDecision(10L, request)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertTrue(ex.reason!!.contains("Only pending decisions can be changed"))
    }

    @Test
    fun `applyDecision rejects expense when decision is REJECTED`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.REJECTED, comment = "Too costly")
        val uploader = User(name = "Emp", surname = "E", username = "emp", passwordHash = "h", role = Role.EMPLOYEE)
        val decider = User(name = "CFO", surname = "C", username = "cfo", passwordHash = "h", role = Role.CFO)

        val decision = ExpenseDecisions(id = 20L, user = decider, decision = Status.PENDING)
        val expense = Expense(
            id = 1L,
            user = uploader,
            ammountDollars = 5000L,
            reason = "Laptop",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { reqDecisionRepo.findByUserIdAndExpenseId(10L, 1L) } returns decision
        every { requestsRepo.findById(1L) } returns Optional.of(expense)

        val result = expensesService.applyDecision(10L, request)

        assertEquals(Status.REJECTED, decision.decision)
        assertEquals("Too costly", decision.comment)
        assertEquals(Status.REJECTED, expense.status)
        assertEquals(Status.REJECTED, result.status)
        verify(exactly = 0) { eventPublisher.publishEvent(any()) }
    }

    @Test
    fun `applyDecision approves expense and publishes event when all approvers agree`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.APPROVED, comment = "Approved")
        val uploader = User(name = "Emp", surname = "E", username = "emp", passwordHash = "h", role = Role.EMPLOYEE)
        val decider = User(name = "CEO", surname = "C", username = "ceo", passwordHash = "h", role = Role.CEO)

        val currentDecision = ExpenseDecisions(id = 100L, user = decider, decision = Status.PENDING)
        val otherDecision = ExpenseDecisions(
            id = 101L,
            user = User(name = "CFO", surname = "F", username = "cfo", passwordHash = "h", role = Role.CFO),
            decision = Status.APPROVED
        )

        val expense = Expense(
            id = 1L,
            user = uploader,
            ammountDollars = 200000L,
            reason = "Office Expansion",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { reqDecisionRepo.findByUserIdAndExpenseId(10L, 1L) } returns currentDecision
        every { requestsRepo.findById(1L) } returns Optional.of(expense)
        every { reqDecisionRepo.findAllByExpenseIdAndIdNot(1L, 100L) } returns listOf(otherDecision)

        val result = expensesService.applyDecision(10L, request)

        assertEquals(Status.APPROVED, currentDecision.decision)
        assertEquals(Status.APPROVED, expense.status)
        assertEquals(Status.APPROVED, result.status)

        verify(exactly = 1) {
            eventPublisher.publishEvent(match<ExpenseApprovedEvent> {
                it.expenseId == 1L && it.amount == 200000L
            })
        }
    }

    @Test
    fun `veto updates expense status to VETOED`() {
        val request = VetoExpense(
            expenseId = 44L,
            comment = "any coomment"
        )
        val expense = Expense(
            id = 44L,
            user = User(name = "Emp", surname = "E", username = "emp", passwordHash = "h", role = Role.EMPLOYEE),
            ammountDollars = 1000L,
            reason = "Monitor",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { requestsRepo.findById(44L) } returns Optional.of(expense)

        expensesService.veto(1L, request)

        assertEquals(Status.VETOED, expense.status)
    }

    @Test
    fun `create sets up approval decisions for CEO role`() {
        val cfo = User(name = "ChiefFinancial", surname = "F", username = "CFO", passwordHash = "h", role = Role.CFO)
        val bordir = User(name = "BoardDirector", surname = "B", username = "BORDIR", passwordHash = "h", role = Role.BOARD_DIRECTOR)
        val ceo = User(name = "ChiefExecutive", surname = "E", username = "CEO", passwordHash = "h", role = Role.CEO)

        val request = ExpenseCreateRequest(ammountDollars = 50000L, reason = "Annual Offsite")

        every { userRepo.getReferenceById(1L) } returns ceo
        every { userRepo.findByUsername("CEO") } returns Optional.of(ceo)
        every { userRepo.findByUsername("CFO") } returns Optional.of(cfo)
        every { userRepo.findByUsername("BORDIR") } returns Optional.of(bordir)

        every { requestsRepo.save(any()) } answers {
            val item = firstArg<Expense>()
            item.id = 500L
            item
        }
        every { reqDecisionRepo.save(any()) } answers { firstArg() }

        val response = expensesService.create(1L, request)

        assertEquals(500L, response.id)
        assertEquals(50000L, response.ammountOfDollars)
        assertEquals(Status.PENDING, response.status)
        assertEquals(listOf("ChiefFinancial", "BoardDirector"), response.needsApprovalBy)

        verify(exactly = 2) { reqDecisionRepo.save(any()) }
    }

    @Test
    fun `create assigns CEO and CFO for high dollar expense by standard employee`() {
        val cfo = User(name = "ChiefFinancial", surname = "F", username = "CFO", passwordHash = "h", role = Role.CFO)
        val bordir = User(name = "BoardDirector", surname = "B", username = "BORDIR", passwordHash = "h", role = Role.BOARD_DIRECTOR)
        val ceo = User(name = "ChiefExecutive", surname = "E", username = "CEO", passwordHash = "h", role = Role.CEO)
        val employee = User(name = "Staff", surname = "S", username = "staff", passwordHash = "h", role = Role.EMPLOYEE)

        val request = ExpenseCreateRequest(ammountDollars = 15000000L, reason = "Acquisition")

        every { userRepo.getReferenceById(2L) } returns employee
        every { userRepo.findByUsername("CEO") } returns Optional.of(ceo)
        every { userRepo.findByUsername("CFO") } returns Optional.of(cfo)
        every { userRepo.findByUsername("BORDIR") } returns Optional.of(bordir)

        every { requestsRepo.save(any()) } answers {
            val item = firstArg<Expense>()
            item.id = 600L
            item
        }
        every { reqDecisionRepo.save(any()) } answers { firstArg() }

        val response = expensesService.create(2L, request)

        assertEquals(listOf("ChiefExecutive", "ChiefFinancial"), response.needsApprovalBy)
        verify(exactly = 2) { reqDecisionRepo.save(any()) }
    }
}

