package com.fdtracker.core.data.smtp

import com.fdtracker.core.domain.repository.SmtpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

@Singleton
class SmtpEmailService @Inject constructor(
    private val smtpRepository: SmtpRepository
) {
    suspend fun sendMaturityReminder(
        fdNumber: String,
        bankName: String,
        maturityDate: LocalDate,
        maturityAmount: BigDecimal,
        daysBefore: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val config = smtpRepository.getConfig()
                ?: return@withContext Result.failure(Exception("SMTP not configured"))

            val props = Properties().apply {
                put("mail.smtp.host", config.host)
                put("mail.smtp.port", config.port.toString())
                put("mail.smtp.auth", "true")
                if (config.useTls) {
                    put("mail.smtp.starttls.enable", "true")
                } else {
                    put("mail.smtp.ssl.enable", "true")
                }
                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(config.username, config.password)
                }
            })

            val dateStr = maturityDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            val subject = when {
                daysBefore > 0 -> "FD Maturity Reminder: $bankName FD maturing in $daysBefore days"
                daysBefore == 0 -> "FD Maturity Today: $bankName FD #$fdNumber"
                else -> "FD Grace Period Ending: $bankName FD #$fdNumber"
            }

            val body = buildHtmlBody(fdNumber, bankName, dateStr, maturityAmount, daysBefore)

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.fromAddress))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.toAddress))
                setSubject(subject)
                setContent(body, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildHtmlBody(
        fdNumber: String,
        bankName: String,
        maturityDate: String,
        maturityAmount: BigDecimal,
        daysBefore: Int
    ): String {
        val statusText = when {
            daysBefore > 0 -> "Your Fixed Deposit is maturing in <strong>$daysBefore days</strong>."
            daysBefore == 0 -> "Your Fixed Deposit <strong>matures today</strong>!"
            else -> "The <strong>grace period is ending</strong> for your matured FD."
        }

        return """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #1a73e8;">FD Tracker - Maturity Reminder</h2>
                <p>$statusText</p>
                <table style="border-collapse: collapse; width: 100%; max-width: 400px;">
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Bank</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">$bankName</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>FD Number</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">$fdNumber</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Maturity Date</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">$maturityDate</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Maturity Amount</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">₹${maturityAmount.toPlainString()}</td></tr>
                </table>
                <p style="color: #666; margin-top: 20px;">— Sent by FD Tracker App</p>
            </body>
            </html>
        """.trimIndent()
    }
}
