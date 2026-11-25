package com.multisig.master.repository

import com.multisig.master.model.PaymentFileDecisions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentFileDecisionRepository : JpaRepository<PaymentFileDecisions, Long> {

    fun findByUserIdAndPaymentFileId(userId: Long, paymentFileId: Long): PaymentFileDecisions?
    fun existsByUserIdAndPaymentFileId(userId: Long, paymentFileId: Long): Boolean

    fun findAllByPaymentFileId(paymentFileId: Long): List<PaymentFileDecisions>

    fun findAllByPaymentFileIdAndIdNot(
        paymentFileId: Long,
        paymentFileDecisionIdToExclude: Long
    ): List<PaymentFileDecisions>

    fun findAllByUserIdAndPaymentFileIdIn(userId: Long, paymentIds: List<Long>): List<PaymentFileDecisions>
    fun findAllByPaymentFileIdIn(payments: List<Long>): List<PaymentFileDecisions>           

}