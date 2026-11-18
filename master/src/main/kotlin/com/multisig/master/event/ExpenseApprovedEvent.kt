package com.multisig.master.event

data class ExpenseApprovedEvent(
    val expenseId: Long,
    val amount: Long
)
