package com.multisig.master.service

import com.multisig.master.components.CSVFileCheker
import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.VetoPayment
import com.multisig.master.event.PaymentFileApprovedEvent
import com.multisig.master.model.PaymentFile
import com.multisig.master.model.PaymentFileDecisions
import com.multisig.master.model.Role
import com.multisig.master.model.Status
import com.multisig.master.model.User
import com.multisig.master.repository.PaymentFileDecisionRepository
import com.multisig.master.repository.PaymentFileRepository
import com.multisig.master.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

class PaymentFileServiceTest {

    private val paymentFileRepo = mockk<PaymentFileRepository>()
    private val reqDecisionRepo = mockk<PaymentFileDecisionRepository>()
    private val userRepo = mockk<UserRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val csvEvaluator = mockk<CSVFileCheker>()

    private lateinit var paymentFileService: PaymentFileService

    @BeforeEach
    fun setUp() {
        paymentFileService = PaymentFileService(
            paymentFileRepo,
            reqDecisionRepo,
            userRepo,
            eventPublisher,
            csvEvaluator
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `applyDecision throws 403 Forbidden when trying to set decision to PENDING`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.PENDING, comment = null)

        val ex = assertThrows<ResponseStatusException> {
            paymentFileService.applyDecision(10L, request)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertTrue(ex.reason!!.contains("Cannot change status to PENDING"))
        verify(exactly = 0) { reqDecisionRepo.findByUserIdAndPaymentFileId(any(), any()) }
    }

    @Test
    fun `applyDecision throws 404 Not Found when user has no decision assigned for this file`() {
        val request = DecideExpense(expenseId = 99L, decision = Status.APPROVED, comment = null)
        every { reqDecisionRepo.findByUserIdAndPaymentFileId(10L, 99L) } returns null

        val ex = assertThrows<ResponseStatusException> {
            paymentFileService.applyDecision(10L, request)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun `applyDecision throws 403 Forbidden when decision is already non-pending`() {

        val request = DecideExpense(expenseId = 1L, decision = Status.APPROVED, comment = null)
        val existingDecision = PaymentFileDecisions(
            id = 50L,
            user = User(name = "Approver", surname = "User", username = "approver", passwordHash = "h", role = Role.MANAGER),
            decision = Status.APPROVED 
        )

        every { reqDecisionRepo.findByUserIdAndPaymentFileId(10L, 1L) } returns existingDecision

        val ex = assertThrows<ResponseStatusException> {
            paymentFileService.applyDecision(10L, request)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertTrue(ex.reason!!.contains("Only pending decisions can be changed"))
    }

    @Test
    fun `applyDecision rejects the entire PaymentFile if decider chooses REJECTED`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.REJECTED, comment = "Exceeds budget")
        val uploader = User(name = "Employee", surname = "Test", username = "emp", passwordHash = "h", role = Role.EMPLOYEE)
        val decider = User(name = "Manager", surname = "Boss", username = "mgr", passwordHash = "h", role = Role.MANAGER)

        val targetDecision = PaymentFileDecisions(
            id = 200L,
            user = decider,
            decision = Status.PENDING
        )

        val paymentFile = PaymentFile(
            id = 1L,
            user = uploader,
            description = "Travel expense",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            filePath = "files/test.csv",
            checkSum = byteArrayOf()
        )

        every { reqDecisionRepo.findByUserIdAndPaymentFileId(10L, 1L) } returns targetDecision
        every { paymentFileRepo.findById(1L) } returns Optional.of(paymentFile)

        val result = paymentFileService.applyDecision(10L, request)

        assertEquals(Status.REJECTED, targetDecision.decision)
        assertEquals("Exceeds budget", targetDecision.comment)
        assertEquals(Status.REJECTED, paymentFile.status)
        assertEquals(Status.REJECTED, result.status)

        verify(exactly = 0) { eventPublisher.publishEvent(any()) }
    }

    @Test
    fun `applyDecision marks PaymentFile as APPROVED and publishes event when all approvers agree`() {
        val request = DecideExpense(expenseId = 1L, decision = Status.APPROVED, comment = "LGTM")
        val uploader = User(name = "Uploader", surname = "U", username = "up", passwordHash = "h", role = Role.EMPLOYEE)
        val currentDecider = User(name = "CFO_User", surname = "C", username = "cfo", passwordHash = "h", role = Role.CFO)

        val currentDecision = PaymentFileDecisions(
            id = 301L,
            user = currentDecider,
            decision = Status.PENDING
        )

        val paymentFile = PaymentFile(
            id = 1L,
            user = uploader,
            description = "Quarterly Software Subscriptions",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            filePath = "files/1_subscriptions.csv",
            checkSum = byteArrayOf()
        )

        val otherDecision = PaymentFileDecisions(
            id = 302L,
            user = User(name = "CEO_User", surname = "O", username = "ceo", passwordHash = "h", role = Role.CEO),
            decision = Status.APPROVED
        )

        every { reqDecisionRepo.findByUserIdAndPaymentFileId(CURRENT_DECIDER_ID, 1L) } returns currentDecision
        every { paymentFileRepo.findById(1L) } returns Optional.of(paymentFile)
        every { reqDecisionRepo.findAllByPaymentFileIdAndIdNot(1L, 301L) } returns listOf(otherDecision)

        val result = paymentFileService.applyDecision(CURRENT_DECIDER_ID, request)

        assertEquals(Status.APPROVED, currentDecision.decision)
        assertEquals(Status.APPROVED, paymentFile.status)
        assertEquals(Status.APPROVED, result.status)

        verify(exactly = 1) {
            eventPublisher.publishEvent(match<PaymentFileApprovedEvent> {
                it.paymentId == 1L && it.file == paymentFile
            })
        }
    }

    @Test
    fun `veto sets payment file status directly to VETOED`() {
        val vetoRequest = VetoPayment(
            55L,
            comment = "Any comment"
        )
        val file = PaymentFile(
            id = 55L,
            user = User(name = "User", surname = "U", username = "usr", passwordHash = "h", role = Role.EMPLOYEE),
            description = "Equipment",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            filePath = "files/test.csv",
            checkSum = byteArrayOf()
        )

        every { paymentFileRepo.findById(55L) } returns Optional.of(file)

        paymentFileService.veto(vetoRequest)

        assertEquals(Status.VETOED, file.status)
    }

    @Test
    fun `getFileToDownload throws 403 Forbidden when user is not the owner and not an assigned decider`() {
        val owner = User(id = 1L, name = "Owner", surname = "O", username = "owner", passwordHash = "h", role = Role.EMPLOYEE)
        val paymentFile = PaymentFile(
            id = 77L,
            user = owner,
            description = "Budget file",
            status = Status.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            filePath = "files/budget.csv",
            checkSum = byteArrayOf()
        )

        val strangerUserId = 999L

        every { paymentFileRepo.findById(77L) } returns Optional.of(paymentFile)
        every { reqDecisionRepo.existsByUserIdAndPaymentFileId(strangerUserId, 77L) } returns false

        val ex = assertThrows<ResponseStatusException> {
            paymentFileService.getFileToDownload(userId = strangerUserId, fileId = 77L)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertTrue(ex.reason!!.contains("You do not have permission to download this file"))
    }

    companion object {
        private const val CURRENT_DECIDER_ID = 15L
    }
}