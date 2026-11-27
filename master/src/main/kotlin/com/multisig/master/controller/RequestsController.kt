package com.multisig.master.controller

import com.multisig.master.config.annotation.CurrentUser
import com.multisig.master.dto.AuthUser
import jakarta.validation.Valid
import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.PaymentFileUploadRequest
import com.multisig.master.dto.UserExpenseResponse
import com.multisig.master.dto.UserExpenseResponseExtened
import com.multisig.master.dto.UserExpenseResponseWithDecision
import com.multisig.master.dto.UserPaymentFileExtended
import com.multisig.master.dto.UserPaymentFileResponse
import com.multisig.master.dto.UserPaymentFileResponseWithDecision
import com.multisig.master.dto.VetoExpense
import com.multisig.master.dto.VetoPayment
import com.multisig.master.service.ExpensesService
import com.multisig.master.service.PaymentFileService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/requests")
class RequestsController(
    private val expensesService: ExpensesService,
    private val paymentFileService: PaymentFileService
) {

    @PostMapping("/expense/create")
    fun createExpense(
        @CurrentUser user: AuthUser,
        @Valid @RequestBody request: ExpenseCreateRequest
    ): ResponseEntity<UserExpenseResponse> {

        log.info { "Creating expense, request by user" }

        val created = expensesService.create(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PostMapping("/expense/veto")
    fun vetoExpense(
        @CurrentUser user: AuthUser,
        @Valid @RequestBody request: VetoExpense
    ): ResponseEntity<String> {

        val created = expensesService.veto(user.id, request)
        return ResponseEntity.ok("Vetoed")
    }

    @GetMapping("/expense/personal")
    fun getExpenses(@CurrentUser user: AuthUser): ResponseEntity<List<UserExpenseResponse>> {

        val expenses = expensesService.getAllPersonalExpenses(user.id)
        return ResponseEntity.ok(expenses)
    }

    @GetMapping("/expense/all")
    @PreAuthorize("hasAnyAuthority('CEO', 'CFO', 'BOARD_DIRECTOR')")
    fun getAllExpenses(): ResponseEntity<List<UserExpenseResponseExtened>> {

        val expenses = expensesService.getAllExpenses()
        return ResponseEntity.ok(expenses)
    }

    @GetMapping("/expense/decisions")
    fun getExpenseDecisions(@CurrentUser user: AuthUser): ResponseEntity<List<UserExpenseResponseWithDecision>> {

        val expenses = expensesService.getExpensesDecisions(user.id)
        return ResponseEntity.ok(expenses)
    }

    @PostMapping("/expense/decision")
    @PreAuthorize("hasAnyAuthority('CEO', 'CFO', 'BOARD_DIRECTOR', 'MANAGER')")
    fun decideExpense(
        @CurrentUser user: AuthUser, @Valid @RequestBody request: DecideExpense
    ): ResponseEntity<UserExpenseResponseWithDecision> {

        val created = expensesService.applyDecision(user.id, request)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }

    @PostMapping(
        "/payments/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @PreAuthorize("hasAnyAuthority('CEO', 'CFO', 'BOARD_DIRECTOR', 'MANAGER', 'ACCOUNTANT')")
    fun uploadExpenseFile(
        @CurrentUser user: AuthUser,
        @Valid @ModelAttribute request: PaymentFileUploadRequest
    ): ResponseEntity<UserPaymentFileResponse> {
        val fileName = request.file.originalFilename
        val fileBytes = request.file.bytes

        val response = paymentFileService.upload(user.id, request)
        return ResponseEntity.ok(response)
    }


    @GetMapping("/payments/personal")
    fun getPaymentsFile(@CurrentUser user: AuthUser): ResponseEntity<List<UserPaymentFileResponse>> {

        val expenses = paymentFileService.getAllPersonalPayments(user.id)
        return ResponseEntity.ok(expenses)
    }

    @GetMapping("/payments/toDecide")
    fun getPaymentsToDecide(@CurrentUser user: AuthUser): ResponseEntity<List<UserPaymentFileResponseWithDecision>> {

        val payment = paymentFileService.getPaymentsToDecide(user.id)
        return ResponseEntity.ok(payment)
    }


    @GetMapping("/payments/download/{id}")
    fun getPaymentFile(
        @CurrentUser user: AuthUser,
        @PathVariable id: Long
    ): ResponseEntity<Resource> {

        val paymentFileDownload = paymentFileService.getFileToDownload(user.id, id)
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(paymentFileDownload.contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${paymentFileDownload.fileName}\"")
            .header(HttpHeaders.CONTENT_LENGTH, paymentFileDownload.fileSize.toString())
            .body(paymentFileDownload.resource)
    }


    @PostMapping("/payments/decision")
    @PreAuthorize("hasAnyAuthority('CEO', 'CFO', 'BOARD_DIRECTOR', 'MANAGER')")
    fun decidePayment(
        @CurrentUser user: AuthUser, @Valid @RequestBody request: DecideExpense
    ): ResponseEntity<UserPaymentFileResponseWithDecision> {

        val created = paymentFileService.applyDecision(user.id, request)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }

    @GetMapping("/payments/all")
    @PreAuthorize("hasAnyAuthority('CEO', 'CFO', 'BOARD_DIRECTOR')")
    fun getAllPayments(): ResponseEntity<List<UserPaymentFileExtended>> {

        val expenses = paymentFileService.getAllExpenses()
        return ResponseEntity.ok(expenses)
    }


    @PostMapping("/payments/veto")
    @PreAuthorize("hasAnyAuthority('CEO', 'CFO', 'BOARD_DIRECTOR')")

    fun vetoPayment(
        @CurrentUser user: AuthUser,
        @Valid @RequestBody request: VetoPayment
    ): ResponseEntity<String> {

        val created = paymentFileService.veto(request)
        return ResponseEntity.ok("Vetoed")
    }

}