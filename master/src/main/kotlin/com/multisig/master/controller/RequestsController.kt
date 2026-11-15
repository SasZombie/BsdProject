package com.multisig.master.controller

import com.multisig.master.dto.ExpenseCreateRequest
import com.multisig.master.dto.ExpenseCreateResponse
import com.multisig.master.dto.ExpnseResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserExpenseResponse
import com.multisig.master.dto.UserResponse
import com.multisig.master.service.RequestService
import com.multisig.master.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/requests")
class RequestsController(private val requestService: RequestService) {

    @PostMapping("/expense/create")
    fun createPayment(@Valid @RequestBody request: ExpenseCreateRequest): ResponseEntity<ExpenseCreateResponse> {
        val created = requestService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
}