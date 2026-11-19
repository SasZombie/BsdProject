package com.multisig.master.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import java.time.Instant


@Entity
@Table(name = "payment_files")
class PaymentFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,
    
    //Instant = UTC time stamp
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,

    @Column
    var description: String? = "",

    @Column(nullable = false)
    var filePath: String = "",

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(columnDefinition = "status", nullable = false)
    var status: Status = Status.PENDING,

    @Column(name = "checksum", nullable = false, updatable = false)
    var checkSum: ByteArray = byteArrayOf(32)
) {
    constructor() : this(id = null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}