package com.multisig.master.controller

import com.multisig.master.config.annotation.CurrentUser
import com.multisig.master.dto.AuthUser
import com.multisig.master.service.PaymentService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/enable")
    fun enable(@CurrentUser user: AuthUser, @RequestHeader("Authorization") authHeader: String) {
        log.info { "Enabling payments" }
        paymentService.toggleService(true)

    }

    @PostMapping("/disable")
    fun disable(@CurrentUser user: AuthUser, @RequestHeader("Authorization") authHeader: String) {
        log.info { "Disabling payments" }
        
        paymentService.toggleService(false)
    }
}
