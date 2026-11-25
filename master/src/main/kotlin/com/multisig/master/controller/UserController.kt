package com.multisig.master.controller

import com.multisig.master.dto.UserBackednResponse
import com.multisig.master.dto.UserCreateRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.dto.UserUpdateRequest
import com.multisig.master.service.PaymentService
import com.multisig.master.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    
    @GetMapping("/all")
    fun getAllUsers(): ResponseEntity<List<UserBackednResponse>> {
        val user = userService.getAllUsers()
        return ResponseEntity.ok(user)
    }
    
    @PostMapping("/update/{id}")
    fun updateUser(@PathVariable id: Long, @Valid @RequestBody newFields: UserUpdateRequest): ResponseEntity<UserBackednResponse>
    {
        log.info { "Updating User with id $id" }
        
        var user = userService.updateUser(id, newFields)
        return ResponseEntity.ok(user)
    }
    
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserLogInResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/create")
    fun createUser(@Valid @RequestBody request: UserCreateRequest): ResponseEntity<UserBackednResponse> {
        
        log.info { "Creating User with id " }
        
        
        val created = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
}