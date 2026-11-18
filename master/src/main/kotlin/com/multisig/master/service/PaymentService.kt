package com.multisig.master.service

import com.multisig.master.event.ExpenseApprovedEvent
import com.multisig.master.model.Expense
import com.multisig.master.model.JournalStage
import com.multisig.master.model.PaymentJournal
import com.multisig.master.model.Status
import com.multisig.master.repository.PaymentJournalRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation


@Service
class PaymentWorkflowService(private val journalRepo: PaymentJournalRepository) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordStart(expenseId: Long): Long {
        val entry = PaymentJournal(
            expenseId = expenseId,
            fromStatus = JournalStage.PAYMENT_PENDING,
            toStatus = JournalStage.DONE,
            stage = JournalStage.STARTED
        )
        return journalRepo.save(entry).id
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordDone(journalId: Long) {
        val entry = journalRepo.findById(journalId).orElseThrow()
        entry.stage = JournalStage.DONE
    }

    fun executeSafePayment(event: ExpenseApprovedEvent) {
        val journalId = recordStart(event.expenseId)

        try {
            println(
                "Here we execute a payment. A simple API would be stripe " +
                        "However it adds a lot of friction and 'grunt work' for this project" +
                        " since for it to work, another db with transaction info must be implemented " +
                        " and each user to have balance and so on, that, in my opinon, does not add anything " +
                        "meaningful to the scope of the project"
            )
            recordDone(journalId)
        } catch (ex: Exception) {
            markFailed(journalId)
            throw ex
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markFailed(journalId: Long) {
        val entry = journalRepo.findById(journalId).orElseThrow()
        entry.stage = JournalStage.FAILED
    }
}