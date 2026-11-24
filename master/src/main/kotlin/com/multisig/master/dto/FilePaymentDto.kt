package com.multisig.master.dto

import com.multisig.master.model.Status
import jakarta.validation.constraints.Size
import org.springframework.core.io.FileSystemResource
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

data class UserPaymentFileResponse(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val needsApprovalBy: List<String>,
    val approvedBy: List<String>,
    val rejectedBy: List<String>,
)

data class UserPaymentFileExtended(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val needsApprovalBy: List<String>,
    val approvedBy: List<String>,
    val rejectedBy: List<String>,
    val postedBy: String
)


data class VetoPayment(
    val expenseId: Long,
    val comment: String?
)

data class UserPaymentFileResponseWithDecision(
    val id: Long,
    val reason: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: Status,
    val personalStatus: Status,
    val userName: String
)

data class PaymentFileDownload(
    val contentType: String,
    val fileName: String,
    val fileSize: Long,
    val resource: FileSystemResource
)

// data class PaymentFileResponseFull
// (
    
// )