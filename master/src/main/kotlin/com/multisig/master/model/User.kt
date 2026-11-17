package com.multisig.master.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant


@Entity
@Table(name = "users")
open class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var name: String = "",

    @Column(nullable = false)
    open var surname: String = "",

    @Column(nullable = false)
    open var username: String = "",

    //Instant = UTC time stamp
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    open var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    open var updatedAt: Instant? = null,

    @Column(nullable = false)
    open var passwordHash: String = "",
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_to_id")
    open var reportsTo: User? = null,
    
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(columnDefinition = "role", nullable = false)
    open var role: Role = Role.ACCOUNTANT
) {
    constructor() : this(id = null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}