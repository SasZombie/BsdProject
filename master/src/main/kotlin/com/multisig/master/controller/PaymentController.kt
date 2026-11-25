package com.multisig.master.controller

import com.multisig.master.service.PaymentService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(private val paymentService: PaymentService) {

    //TODO: Add Haldint to Front-End
    @PostMapping("/enable")
    fun enable(@RequestHeader("Authorization") authHeader: String) {
        paymentService.toggleService(true)

    }

    @PostMapping("/disable")
    fun disable(@RequestHeader("Authorization") authHeader: String) {
        paymentService.toggleService(false)
    }
}
