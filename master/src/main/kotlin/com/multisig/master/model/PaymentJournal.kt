package com.multisig.master.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType

enum class JournalStage {
    PAYMENT_PENDING,
    STARTED,
    DONE,
    FAILED
}

@Entity
@Table(name = "payment_journal")
class PaymentJournal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "expense_id", nullable = false)
    var expenseId: Long = 0,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(columnDefinition = "from_status", nullable = false)
    var fromStatus: JournalStage = JournalStage.PAYMENT_PENDING,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(columnDefinition = "to_status", nullable = false)
    var toStatus: JournalStage = JournalStage.PAYMENT_PENDING,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(columnDefinition = "current_stage", nullable = false)
    var stage: JournalStage = JournalStage.PAYMENT_PENDING,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0
)