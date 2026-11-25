package com.multisig.master.controller

import com.multisig.master.dto.UserLogInRequest
import com.multisig.master.dto.UserLogInResponse
import com.multisig.master.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLogInRequest): ResponseEntity<UserLogInResponse> {
        val authResponse = authService.login(request)
        return ResponseEntity.ok(authResponse)
    }
    
    @GetMapping("/refreshToken")
    fun refreshToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<String> {
        val token = authHeader.removePrefix("Bearer ").trim()
        
        val authResponse = authService.refreshToken(token)
        return ResponseEntity.ok(authResponse)
    }
}