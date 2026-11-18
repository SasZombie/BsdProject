package com.multisig.master.listener

import com.multisig.master.event.ExpenseApprovedEvent
import com.multisig.master.service.PaymentService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(private val paymentService: PaymentService) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleExpenseFullyApproved(event: ExpenseApprovedEvent) {
       
        paymentService.executeSafePayment(event)
    }
}