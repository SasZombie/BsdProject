package com.multisig.master.repository

import com.multisig.master.model.PaymentFile
import com.multisig.master.model.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PaymentFileRepository : JpaRepository<PaymentFile, Long> {
    fun findAllByUserId(userId: Long): List<PaymentFile>

    @Query("""
        SELECT pf.paymentFile 
        FROM PaymentFileDecisions pf 
        WHERE pf.user.id = :userId 
          AND pf.decision = :status
        """)
    fun findPendingPaymentFilesByUserId(@Param("userId") userId: Long, @Param("status") status: Status = Status.PENDING): List<PaymentFile>
    
    @Query("""
        SELECT ed.paymentFile 
        FROM PaymentFileDecisions ed 
        WHERE ed.user.id = :userId 
        """)
    fun findPaymentFilesByUserId(@Param("userId") userId: Long): List<PaymentFile>
    
     @Query("""
        SELECT pf.paymentFile 
        FROM PaymentFileDecisions pf 
        WHERE pf.user.id = :userId 
          AND pf.decision = :status
        """)
    fun findPaymentFileByUserIdAndFileId(@Param("userId") userId: Long, @Param("status") status: Status = Status.PENDING): List<PaymentFile>
}