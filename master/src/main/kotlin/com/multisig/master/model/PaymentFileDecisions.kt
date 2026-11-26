package com.multisig.master.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import java.math.BigInteger
import java.time.Instant


@Entity
@Table(name = "payment_files_decisions")
class PaymentFileDecisions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_file_id", nullable = false)
    var paymentFile: PaymentFile? = null,
    //Instant = UTC time stamp
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,

    @Column
    var comment: String? = "",

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(name="decision", columnDefinition = "status", nullable = false)
    var decision: Status = Status.PENDING,

) {
    constructor() : this(id = null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}