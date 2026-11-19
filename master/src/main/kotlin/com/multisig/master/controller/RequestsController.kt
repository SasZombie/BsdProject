package com.multisig.master.controller

import jakarta.validation.Valid
import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.ExpenseCreateResponse
import com.multisig.master.dto.PaymentFileUploadRequest
import com.multisig.master.dto.UserExpenseResponse
import com.multisig.master.dto.UserExpenseResponseWithDecision
import com.multisig.master.dto.UserPaymentFileUploadResponse
import com.multisig.master.service.ExpensesService
import com.multisig.master.service.PaymentFileService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/requests")
class RequestsController(
    private val requestService: ExpensesService,
    private val paymentFileService: PaymentFileService
) {

    @PostMapping("/expense/create")
    fun createPayment(@Valid @RequestBody request: ExpenseCreateRequest): ResponseEntity<ExpenseCreateResponse> {
        val created = requestService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/expense/personal")
    fun getPayments(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<UserExpenseResponse>> {
        val token = authHeader.removePrefix("Bearer ").trim()

        val expenses = requestService.getAllExpenses(token)
        return ResponseEntity.ok(expenses)
    }

    @GetMapping("/expense/toDecide")
    fun getAllPayments(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<UserExpenseResponse>> {
        val token = authHeader.removePrefix("Bearer ").trim()

        val expenses = requestService.getExpensesToDecide(token)
        return ResponseEntity.ok(expenses)
    }

    @PostMapping("/expense/decision")
    fun decideExpense(
        @RequestHeader("Authorization") authHeader: String, @Valid @RequestBody request: DecideExpense
    ): ResponseEntity<UserExpenseResponseWithDecision> {
        val token = authHeader.removePrefix("Bearer ").trim()

        val created = requestService.applyDecision(token, request)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }

    
    @PostMapping(
        "/payments/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadExpenseFile( @RequestHeader("Authorization") authHeader: String, @Valid @ModelAttribute request: PaymentFileUploadRequest): ResponseEntity<UserPaymentFileUploadResponse> {
        val fileName = request.file.originalFilename
        val fileBytes = request.file.bytes

        val token = authHeader.removePrefix("Bearer ").trim()
        
        val response = paymentFileService.upload(token.toLong(), request)
        return ResponseEntity.ok(response)
    }
}