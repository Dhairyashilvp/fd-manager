package com.fdtracker.core.domain.usecase.ocr

import com.fdtracker.core.domain.model.OcrParsedFd

class ParseOcrResultUseCase {
    operator fun invoke(rawText: String): OcrParsedFd {
        val lines = rawText.lines().map { it.trim() }
        val fullText = rawText.uppercase()

        return OcrParsedFd(
            bankName = extractBankName(fullText, lines),
            fdAccountNumber = extractPattern(
                fullText,
                listOf(
                    """FD\s*(?:NO|NUMBER|A/C|ACCOUNT)\s*[:\-]?\s*([A-Z0-9\-/]+)""",
                    """(?:DEPOSIT|RECEIPT)\s*(?:NO|NUMBER)\s*[:\-]?\s*([A-Z0-9\-/]+)""",
                    """FDR\s*(?:NO|NUMBER)\s*[:\-]?\s*([A-Z0-9\-/]+)"""
                )
            ),
            principalAmount = extractAmount(fullText, listOf("PRINCIPAL", "DEPOSIT AMOUNT", "BOOKING AMOUNT")),
            interestRate = extractPattern(
                fullText,
                listOf(
                    """(?:RATE|ROI|INTEREST)\s*(?:OF\s*INTEREST)?\s*[:\-]?\s*(\d+\.?\d*)\s*%""",
                    """(\d+\.?\d*)\s*%\s*(?:P\.?A|PER\s*ANNUM)"""
                )
            ),
            valueDate = extractDate(fullText, listOf("DATE OF DEPOSIT", "VALUE DATE", "BOOKING DATE", "START DATE")),
            maturityDate = extractDate(fullText, listOf("MATURITY DATE", "DUE DATE", "EXPIRY DATE")),
            tenure = extractPattern(
                fullText,
                listOf(
                    """TENURE\s*[:\-]?\s*(\d+\s*(?:DAYS?|MONTHS?|YEARS?))""",
                    """PERIOD\s*[:\-]?\s*(\d+\s*(?:DAYS?|MONTHS?|YEARS?))"""
                )
            ),
            maturityAmount = extractAmount(fullText, listOf("MATURITY AMOUNT", "MATURITY VALUE", "PAYABLE AMOUNT")),
            holderName = extractPattern(
                fullText,
                listOf(
                    """(?:NAME|HOLDER|DEPOSITOR)\s*[:\-]?\s*([A-Z\s]+?)(?:\n|$)""",
                    """(?:MR|MRS|MS|SHRI|SMT)\s*\.?\s*([A-Z\s]+?)(?:\n|$)"""
                )
            )?.trim(),
            confidence = calculateConfidence(fullText)
        )
    }

    private fun extractBankName(text: String, lines: List<String>): String? {
        val bankKeywords = listOf(
            "STATE BANK", "SBI", "HDFC", "ICICI", "AXIS", "KOTAK", "PNB",
            "BANK OF BARODA", "BOB", "CANARA", "UNION BANK", "IDBI",
            "INDIAN BANK", "CENTRAL BANK", "BANK OF INDIA", "BOI",
            "YES BANK", "FEDERAL BANK", "SOUTH INDIAN BANK", "BANDHAN"
        )
        for (keyword in bankKeywords) {
            if (text.contains(keyword)) {
                return keyword.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
            }
        }
        // Try first line as bank name
        return lines.firstOrNull { it.length in 3..50 }
    }

    private fun extractPattern(text: String, patterns: List<String>): String? {
        for (pattern in patterns) {
            val match = Regex(pattern, RegexOption.IGNORE_CASE).find(text)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }

    private fun extractAmount(text: String, labels: List<String>): String? {
        for (label in labels) {
            val pattern = """$label\s*[:\-]?\s*(?:RS\.?|₹|INR)?\s*([\d,]+\.?\d*)"""
            val match = Regex(pattern, RegexOption.IGNORE_CASE).find(text)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].replace(",", "").trim()
            }
        }
        // General amount pattern
        val amountPattern = """(?:RS\.?|₹|INR)\s*([\d,]+\.?\d*)"""
        val matches = Regex(amountPattern, RegexOption.IGNORE_CASE).findAll(text).toList()
        return matches.maxByOrNull { it.groupValues[1].replace(",", "").toDoubleOrNull() ?: 0.0 }
            ?.groupValues?.get(1)?.replace(",", "")
    }

    private fun extractDate(text: String, labels: List<String>): String? {
        for (label in labels) {
            val pattern = """$label\s*[:\-]?\s*(\d{1,2}[/\-\.]\d{1,2}[/\-\.]\d{2,4})"""
            val match = Regex(pattern, RegexOption.IGNORE_CASE).find(text)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }

    private fun calculateConfidence(text: String): Float {
        var score = 0f
        val checks = listOf(
            "FD" to 0.1f,
            "DEPOSIT" to 0.1f,
            "PRINCIPAL" to 0.1f,
            "MATURITY" to 0.1f,
            "INTEREST" to 0.1f,
            "BANK" to 0.1f,
            "RATE" to 0.05f,
            "TENURE" to 0.05f,
            "DATE" to 0.05f,
            "₹" to 0.1f,
            "%" to 0.1f
        )
        for ((keyword, weight) in checks) {
            if (text.contains(keyword)) score += weight
        }
        return score.coerceIn(0f, 1f)
    }
}
