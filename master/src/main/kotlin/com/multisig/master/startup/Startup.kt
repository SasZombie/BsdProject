package com.multisig.master.startup

import com.multisig.master.service.PaymentService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class PaymentRecovery(
    private val paymentService: PaymentService
) : CommandLineRunner {

    override fun run(vararg args: String) {
        
        paymentService.redoIfNeeded()
    }
}