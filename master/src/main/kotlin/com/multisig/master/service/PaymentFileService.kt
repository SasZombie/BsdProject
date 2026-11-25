package com.multisig.master.service

import com.multisig.master.dto.DecideExpense
import com.multisig.master.dto.PaymentFileDownload
import com.multisig.master.dto.PaymentFileUploadRequest
import com.multisig.master.dto.UserPaymentFileExtended
import com.multisig.master.dto.UserPaymentFileResponseWithDecision
import com.multisig.master.dto.UserPaymentFileResponse
import com.multisig.master.dto.VetoPayment
import com.multisig.master.event.PaymentFileApprovedEvent
import com.multisig.master.model.PaymentFile
import com.multisig.master.model.PaymentFileDecisions
import com.multisig.master.model.Role
import com.multisig.master.model.Status
import com.multisig.master.model.User
import com.multisig.master.repository.PaymentFileDecisionRepository
import com.multisig.master.repository.PaymentFileRepository
import com.multisig.master.repository.UserRepository
import com.opencsv.CSVReaderHeaderAware
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Instant

@Service
class PaymentFileService(
    private val paymentFileRepo: PaymentFileRepository,
    private val reqDecisionRepo: PaymentFileDecisionRepository,
    private val userRepo: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val filesFolder = "files/"
    private var unkownCount = 0

    @Transactional(readOnly = true)
    fun getAllPersonalPayments(id: String): List<UserPaymentFileResponse> {

        val requests = paymentFileRepo.findAllByUserId(id.toLong())
        if (requests.isEmpty()) return emptyList()

        val requestIds = requests.mapNotNull { it.id }
        //Map<ExpenseID -> ListOfExpenseDecisions>
        val decisionsByRequestId = reqDecisionRepo.findAllByPaymentFileIdIn(requestIds)
            .groupBy { it.paymentFile!!.id }

        return requests.map { req ->
            val decisions = decisionsByRequestId[req.id].orEmpty()
            val (pendingDecisions, decidedDecisions) = decisions.partition { it.decision == Status.PENDING }
            val (approvedBy, rejectedBy) = decidedDecisions.partition { it.decision == Status.APPROVED }
                .let { (approved, denied) ->
                    approved.map { it.user!!.name } to denied.map { it.user!!.name }
                }

            val pending = pendingDecisions.map { it.user!!.name }
            req.toResponse(pending, approvedBy, rejectedBy = rejectedBy)
        }
    }

    @Transactional(readOnly = true)
    fun getAllExpenses(): List<UserPaymentFileExtended> {
        val requests = paymentFileRepo.findAll()
        if (requests.isEmpty()) return emptyList()

        val requestIds = requests.mapNotNull { it.id }
        //Map<ExpenseID -> ListOfExpenseDecisions>
        val decisionsByRequestId = reqDecisionRepo.findAllByPaymentFileIdIn(requestIds)
            .groupBy { it.paymentFile!!.id }

        return requests.map { req ->
            val decisions = decisionsByRequestId[req.id].orEmpty()
            val (pendingDecisions, decidedDecisions) = decisions.partition { it.decision == Status.PENDING }
            val (approvedBy, rejectedBy) = decidedDecisions.partition { it.decision == Status.APPROVED }
                .let { (approved, denied) ->
                    approved.map { it.user!!.name } to denied.map { it.user!!.name }
                }

            val pending = pendingDecisions.map { it.user!!.name }
            req.toResponseExtened(req.user!!.name, pending, approvedBy, rejectedBy = rejectedBy)
        }

    }

    @Transactional(readOnly = true)
    fun getPaymentsToDecide(id: String): List<UserPaymentFileResponseWithDecision> {
        val userId = id.toLong()
        val payments = paymentFileRepo.findPaymentFilesByUserId(userId)
        if (payments.isEmpty()) return emptyList()

        val expenseIds = payments.mapNotNull { it.id }
        val decisionsByExpenseId: Map<Long, PaymentFileDecisions> = reqDecisionRepo
            .findAllByUserIdAndPaymentFileIdIn(userId, expenseIds)
            .associateBy { it.paymentFile!!.id!! }

        return payments.map { expense ->
            val decision = decisionsByExpenseId[expense.id]?.decision ?: Status.PENDING

            UserPaymentFileResponseWithDecision(
                id = expense.id!!,
                reason = expense.description.orEmpty(),
                createdAt = expense.createdAt!!,
                updatedAt = expense.updatedAt!!,
                status = expense.status,
                personalStatus = decision,
                userName = expense.user?.name.orEmpty()
            )
        }
    }

    @Transactional
    fun applyDecision(id: String, request: DecideExpense): UserPaymentFileResponseWithDecision {

        if (request.decision == Status.PENDING) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Cannot change status to PENDING"
            )
        }

        val decisionQuerry = reqDecisionRepo.findByUserIdAndPaymentFileId(id.toLong(), request.expenseId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Combination $id and ${request.expenseId} not found"
            )

        if (decisionQuerry.decision != Status.PENDING) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only pending decisions can be changed"
            )
        }
        decisionQuerry.decision = request.decision
        if (request.comment != null) {
            decisionQuerry.comment = request.comment
        }

        val initialReq = paymentFileRepo.findById(request.expenseId).orElseThrow {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "ExpenseId not found"
            )
        }
        //If not approved deny the whole request
        if (request.decision != Status.APPROVED) {

            initialReq.status = request.decision
        } else {
            //If approved, see if everyone approved
            val allWhoNeedToApprove =
                reqDecisionRepo.findAllByPaymentFileIdAndIdNot(request.expenseId, decisionQuerry.id!!)

            if (allWhoNeedToApprove.all { it.decision == Status.APPROVED }) {

                initialReq.status = Status.APPROVED

                eventPublisher.publishEvent(
                    PaymentFileApprovedEvent(initialReq.id!!, initialReq)
                )
            }
        }

        val returned = UserPaymentFileResponseWithDecision(
            id = initialReq.id!!,
            reason = initialReq.description!!,
            createdAt = initialReq.createdAt!!,
            updatedAt = Instant.now(),
            status = initialReq.status,
            personalStatus = request.decision,
            userName = initialReq.user!!.name
        )

        return returned
    }

    fun createPath(fileName: String): String {
        if (fileName == "EmptyFileName") {
            unkownCount += 1

            return filesFolder + fileName + unkownCount
        }

        //TODO: This would benefit for more security!!!
        val cleanFileName = fileName.substringAfterLast('/').removePrefix("/")

        return filesFolder + cleanFileName
    }

    private fun saveFileAtPath(path: String, file: MultipartFile) {
        Files.copy(file.inputStream, Path.of(path))
    }

    private fun sha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)

        DigestInputStream(inputStream, digest).use { dis ->
            @Suppress("ControlFlowWithEmptyBody")
            while (dis.read(buffer) != -1) {
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }


    @Transactional
    fun veto(request: VetoPayment) {
        println(
            "This veto function is deeply integrated with the payment system" +
                    "that is not implemented, so I will also skip the boilerplate"
        )

        val expense =
            paymentFileRepo.findById(request.expenseId)
                .orElseThrow { NoSuchElementException("This request doesnt exist") }

        expense.status = Status.VETOED

    }

    @Transactional(readOnly = true)
    fun getFileToDownload(userId: Long, fileId: Long): PaymentFileDownload {

        val paymentFile = paymentFileRepo.findById(fileId)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND, "Payment file not found") }

        val isOwner = paymentFile.user!!.id == userId 

        val hasDecisionAccess = if (!isOwner) {
            reqDecisionRepo.existsByUserIdAndPaymentFileId(userId, fileId)
        } else {
            false
        }

        if (!isOwner && !hasDecisionAccess) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have permission to download this file"
            )
        }

        // val querry = reqDecisionRepo.findByUserIdAndPaymentFileId(userId, fileId) ?: throw ResponseStatusException(
        //     HttpStatus.FORBIDDEN,
        //     "This combination doesnt exist"
        // )

        // if (querry.paymentFile == null) {
        //     throw ResponseStatusException(
        //         HttpStatus.BAD_REQUEST,
        //         "Cannot find this file"
        //     )
        // }

        val filePathString = paymentFile!!.filePath
        val filePath = Paths.get(filePathString)

        val file = filePath.toFile()

        if (!file.exists()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cannot find resource at given path"
            )
        }

        val resource = FileSystemResource(file)
        var contentType = Files.probeContentType(filePath)

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
        }

        return PaymentFileDownload(
            contentType,
            filePathString,
            file.length(),
            resource
        )
    }

    @Transactional
    fun upload(userId: Long, request: PaymentFileUploadRequest): UserPaymentFileResponse {

        //TODO: Maybe Validate File? At least the header I guess
        //TODO: Need a way of storing duplicates. Simplest is appending a file number
        //Hardest is to: Search all files for duplication
        val userProxy = userRepo.getReferenceById(userId)

        val path = createPath(request.file.originalFilename ?: "EmptyFileName")
        val fileBytes = request.file.bytes

        val checkSum = MessageDigest.getInstance("SHA-256")
            .digest(request.file.bytes)
            .joinToString("") { "%02x".format(it) }

        val newPayment = PaymentFile(
            user = userProxy,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            description = request.reason,
            filePath = path,
            checkSum = checkSum.toByteArray(),
            status = Status.PENDING
        )
        val queerry = paymentFileRepo.save(newPayment)
        val usersThatMustApprove = decideApprovals(fileBytes, newPayment)

        Files.write(Path.of(path), fileBytes)

        return queerry.toResponse(usersThatMustApprove.map { user -> user.name }, emptyList(), emptyList())
    }

    private fun sumAmount(fileBytes: ByteArray): Long =
        InputStreamReader(ByteArrayInputStream(fileBytes)).use { reader ->
            CSVReaderHeaderAware(reader).use { csv ->
                generateSequence { csv.readMap() }
                    .mapNotNull { it["Amount"]?.trim()?.takeIf(String::isNotEmpty) }
                    .sumOf { it.toLong() }
            }
        }


    @Transactional
    private fun decideApprovals(bytes: ByteArray, paymentFile: PaymentFile): List<User> {

        val dollars = sumAmount(bytes)

        val ceo = getUserByUsername("CEO")
        val cfo = getUserByUsername("CFO")
        val boardDir = getUserByUsername("BORDIR")

        val usersThatMustApprove = when (paymentFile.user!!.role) {

            Role.CEO -> {
                listOf(cfo, boardDir)

            }

            Role.CFO -> {
                listOf(ceo, boardDir)

            }

            Role.BOARD_DIRECTOR -> {

                listOf(ceo, cfo)
            }

            else -> {

                when {
                    dollars > 100_000_00 -> {
                        listOf(
                            ceo, cfo
                        )
                    }

                    dollars > 10_000_00 -> {

                        val manager1 = paymentFile.user?.reportsTo ?: ceo
                        val manager2 = paymentFile.user?.reportsTo?.reportsTo ?: boardDir

                        listOf(cfo, manager1, manager2).distinct()
                    }

                    dollars > 5_000_00 -> {

                        val manager1 = paymentFile.user?.reportsTo ?: ceo
                        val manager2 = paymentFile.user?.reportsTo?.reportsTo ?: cfo

                        listOf(manager1, manager2)
                    }

                    else -> {
                        println("AIci se executa!")
                        val manager1 = paymentFile.user?.reportsTo ?: cfo
                        println(paymentFile.user?.surname)
                        println(paymentFile.user?.reportsTo?.surname)

                        listOf(manager1)
                    }
                }
            }
        }

        for (userThatMustApprove in usersThatMustApprove) {
            val newDecision = PaymentFileDecisions(
                user = userThatMustApprove,
                paymentFile = paymentFile
            )
            reqDecisionRepo.save(newDecision)
        }

        return usersThatMustApprove
    }


    private fun getUserByUsername(username: String): User =
        userRepo.findByUsername(username)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "$username not found") }


    private fun PaymentFile.toResponse(
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): UserPaymentFileResponse =
        UserPaymentFileResponse(
            id = id!!,
            reason = description ?: "",
            createdAt = createdAt!!,
            updatedAt = updatedAt!!,
            status = status,
            needsApprovalBy = needsApprovalBy,
            approvedBy = approvedBy,
            rejectedBy = rejectedBy
        )

    private fun PaymentFile.toResponseExtened(
        userName: String,
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): UserPaymentFileExtended =
        UserPaymentFileExtended(
            id = id!!,
            reason = description ?: "",
            createdAt = createdAt!!,
            updatedAt = updatedAt!!,
            status = status,
            needsApprovalBy = needsApprovalBy,
            approvedBy = approvedBy,
            rejectedBy = rejectedBy,
            postedBy = userName
        )

    private fun List<PaymentFile>.toResponse(
        needsApprovalBy: List<String>,
        approvedBy: List<String>,
        rejectedBy: List<String>
    ): List<UserPaymentFileResponse> = this.map { it.toResponse(needsApprovalBy, approvedBy, rejectedBy) }
}