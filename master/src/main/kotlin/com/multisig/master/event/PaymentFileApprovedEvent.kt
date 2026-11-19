package com.multisig.master.event

import com.multisig.master.model.PaymentFile

data class PaymentFileApprovedEvent(
    val expenseId: Long,
    val file: PaymentFile 
)
