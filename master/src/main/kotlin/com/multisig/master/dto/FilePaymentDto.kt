package com.multisig.master.dto

import com.multisig.master.model.Status
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class PaymentFileUploadRequest(
    val file: MultipartFile,

    @field:Size(max = 500, message = "Reason cannot exceed 500 characters")
    val reason: String
)


data class PaymentFileUploaddedResponse(
    val message: String,
    val approvalsNeededCount: Int
)

data class DecidePaymentFile(
    val expenseId: Long,
    val decision: Status,
    val comment: String?
)

data class UserPaymentFileUploadResponse(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
)

data class UserPaymentFileResponseWithDecision(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val personalDecision: Status
)