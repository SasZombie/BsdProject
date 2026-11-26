package com.multisig.master.components


import com.opencsv.CSVReader
import com.opencsv.CSVReaderHeaderAware
import com.opencsv.exceptions.CsvException
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

@Component
class CSVFileCheker {

    sealed interface CsvValidationResult {
        data class Valid(val totalAmount: Long, val rowCount: Int) : CsvValidationResult
        data class Invalid(val error: String) : CsvValidationResult
    }

    private val expectedHeaders = listOf("From", "To", "Reason", "Ammount", "Currency")
    private val currencyRegex = Regex("^[A-Z]{3}$")

    fun validateAndSum(fileBytes: ByteArray): CsvValidationResult = runCatching {
        InputStreamReader(ByteArrayInputStream(fileBytes)).use { reader ->
            CSVReader(reader).use { csv ->

                val header = csv.readNext()?.map { it.trim() }
                    ?: return CsvValidationResult.Invalid("File is completely empty")

                if (header != expectedHeaders) {
                    return CsvValidationResult.Invalid(
                        "Invalid header. Expected $expectedHeaders, but found $header"
                    )
                }

                var sum = 0L
                var rowIdx = 1 

                while (true) {
                    val row = csv.readNext() ?: break
                    rowIdx++

                    if (row.size != expectedHeaders.size) {
                        return CsvValidationResult.Invalid(
                            "Line $rowIdx: Expected ${expectedHeaders.size} columns, found ${row.size}"
                        )
                    }

                    val from = row[0].trim()
                    val to = row[1].trim()
                    val reason = row[2].trim()
                    val rawAmount = row[3].trim()
                    val currency = row[4].trim()

                    if (from.isEmpty() || to.isEmpty()) {
                        return CsvValidationResult.Invalid("Line $rowIdx: 'From' and 'To' fields cannot be blank")
                    }

                    val amount = rawAmount.toLongOrNull()
                        ?: return CsvValidationResult.Invalid(
                            "Line $rowIdx: Invalid amount '$rawAmount'. Must be a whole integer."
                        )

                    if (!currencyRegex.matches(currency)) {
                        return CsvValidationResult.Invalid(
                            "Line $rowIdx: Invalid currency code '$currency'. Expected 3 uppercase letters."
                        )
                    }

                    sum += amount
                }

                if (rowIdx == 1) {
                    return CsvValidationResult.Invalid("File contains headers but no transaction rows")
                }

                CsvValidationResult.Valid(totalAmount = sum, rowCount = rowIdx - 1)
            }
        }
    }.getOrElse { ex ->
        when (ex) {
            is CsvException -> CsvValidationResult.Invalid("Malformed CSV syntax: ${ex.message}")
            is java.io.IOException -> CsvValidationResult.Invalid("I/O error: ${ex.message}")
            else -> throw ex
        }
    }
}