package com.multisig.master.controller

import com.multisig.master.config.annotation.CurrentUser
import com.multisig.master.dto.AuthUser
import com.multisig.master.service.AdminService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('ADMIN')")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/logs")
    fun getLogLine(@CurrentUser user: AuthUser): ResponseEntity<List<String>> {
        
        val newLogs = adminService.getNewLogs()
        return ResponseEntity.ok(newLogs)
    }
}