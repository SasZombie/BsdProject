package com.multisig.master.service

import com.multisig.master.event.ExpenseApprovedEvent
import com.multisig.master.model.JournalStage
import com.multisig.master.model.PaymentJournal
import com.multisig.master.repository.PaymentJournalRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import java.util.concurrent.atomic.AtomicBoolean


@Component
class PaymentJournalService(private val journalRepo: PaymentJournalRepository) {

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markFailed(journalId: Long) {
        val entry = journalRepo.findById(journalId).orElseThrow()
        entry.stage = JournalStage.FAILED
    }

    @Transactional(readOnly = true)
    fun getAllNotDone(): List<PaymentJournal> {
        return journalRepo.findAllByStage(JournalStage.STARTED)
    }

}

@Service
class PaymentService(private val journalService: PaymentJournalService) {

    private val isEnabled = AtomicBoolean(true)
    fun toggleService(enabled: Boolean) {
        isEnabled.set(enabled)
    }

    fun isServiceEnabled(): Boolean = isEnabled.get()

    private fun checkServiceStatus() {
        if (!isEnabled.get()) {
            throw RuntimeException("Payment service is temporarily disabled.")
        }
    }

    fun executeSafePayment(event: ExpenseApprovedEvent) {

        checkServiceStatus()

        val journalId = journalService.recordStart(event.expenseId)

        try {
            println(
                "Here we execute a payment. A simple API would be stripe " +
                        "However it adds a lot of friction and 'grunt work' for this project" +
                        " since for it to work, another db with transaction info must be implemented " +
                        " and each user to have balance and so on, that, in my opinon, does not add anything " +
                        "meaningful to the scope of the project"
            )
            journalService.recordDone(journalId)
        } catch (ex: Exception) {
            journalService.markFailed(journalId)
            throw ex
        }
    }

    fun redoIfNeeded() {
        checkServiceStatus()

        val allNotDone = journalService.getAllNotDone()

        for (stage in allNotDone) {
            //Check payment provider
            //If we made the payment
            @Suppress("ConstantConditionIf")
            if (true) {
                journalService.recordDone(stage.id)
            } else {
                try {
                    journalService.recordDone(stage.id)
                } catch (ex: Exception) {
                    journalService.markFailed(stage.id)
                    throw ex
                }
            }
        }
    }
}