package com.multisig.master.dto

import com.multisig.master.model.Role
import com.sun.org.apache.xpath.internal.operations.Bool
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserCreateRequest(
    @field:NotBlank(message = "Creator cannot be blank")
    val token: String,
    
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Surrnae cannot be blank")
    val surname: String,
    
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,
    
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:NotBlank(message = "Password must be at least 8 characters")
    val role: Role
)

data class UserLogInRequest(
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,
    
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
)

data class UserUpdateRequest(
    val reportsTo: String?,
    val role: Role?
)

data class UserLogInResponse(
    val token: String,
    val role: Role
)

data class UserBackednResponse(
    val id: Long,
    val name: String,
    val surname: String,
    val username: String,
    val reportsTo: String?,
    val role: Role
)