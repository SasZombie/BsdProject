package com.multisig.master.dto

import com.multisig.master.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserCreateRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Surrnae cannot be blank")
    val surrname: String,
    
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

data class UserResponse(
    val token: String,
    val name: String,
    val surname: String,
    val username: String,
    val role: Role
)