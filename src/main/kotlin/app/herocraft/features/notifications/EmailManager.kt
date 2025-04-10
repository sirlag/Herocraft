package app.herocraft.features.notifications

import org.apache.commons.mail.SimpleEmail
import org.slf4j.LoggerFactory


class EmailManager(private val properties: NotificationManager.EmailNotifications) : NotificationSender {

    private val logger = LoggerFactory.getLogger(EmailManager::class.java)!!

    override fun sendVerificationEmail(to: String, token: String) {
        val email = newEmail()
        email.setSubject("[Herocraft]: Verify your email address")
        email.setMsg("""
            Hello, Please follow the link below to complete your Herocraft account registration. 
            
            https://herocraft.app/account/verify/$token
            """.trimIndent())
        email.addTo(to)
        email.send()
        logger.info("Verification email sent successfully to $to")
    }

    override fun sendPasswordReset(to: String, username: String, token: String) {
        val email = newEmail()
        email.setSubject("[Herocraft]: Password reset information")
        email.setMsg("""
            Hello $username,
            A password reset was requested for your account. To reset your password, click the link below.
            
            https://herocraft.app/account/reset/$token
            
            This link will expire in around an hour. If you did not request to have your password reset, you can ignore this email.
        """.trimIndent())
        email.addTo(to)
        email.send()
        logger.info("Password reset successfully successfully for $to")
    }

    private fun newEmail(): SimpleEmail {
        val email = SimpleEmail()
        email.hostName = properties.hostName
        email.setSmtpPort(properties.port)
        email.setFrom(properties.sender.email, properties.sender.name)
        email.setAuthentication(properties.sender.username, properties.sender.password)
        email.isStartTLSRequired = properties.security.startTlsRequired
        email.setSSLOnConnect(properties.security.SSLOnConnect)
        return email
    }
}


