package com.multisig.master.repository

import com.multisig.master.model.JournalStage
import com.multisig.master.model.PaymentJournal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentJournalRepository : JpaRepository<PaymentJournal, Long> {
    fun findAllByStage(stage: JournalStage): List<PaymentJournal>
    
    
}