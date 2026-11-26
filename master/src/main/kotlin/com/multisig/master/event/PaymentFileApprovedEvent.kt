package com.multisig.master.event

import com.multisig.master.model.PaymentFile

data class PaymentFileApprovedEvent(
    val paymentId: Long,
    val file: PaymentFile 
)
