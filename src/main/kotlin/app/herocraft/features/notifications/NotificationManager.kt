package app.herocraft.features.notifications

import io.ktor.server.config.*
import org.slf4j.LoggerFactory

class NotificationManager(private val config: ApplicationConfig) {

    data class ConsoleNotifications(val enabled: Boolean = true)
    data class EmailNotifications(val enabled: Boolean = false, val hostName: String?, val port: Int, val sender: EmailSender, val security: EmailSecurity)
    data class EmailSender(val email: String, val name: String, val username: String, val password: String)
    data class EmailSecurity(val startTlsRequired: Boolean, val SSLOnConnect: Boolean)

    private val logger = LoggerFactory.getLogger(NotificationManager::class.java)!!
    private val activeSenders = mutableListOf<NotificationSender>()

    init {
        val consoleNotifications = ConsoleNotifications(config.property("herocraft.notifications.console.enabled").getString().toBoolean() ?: true)
        if (consoleNotifications.enabled) {
            activeSenders.add(ConsoleService())
            logger.debug("ConsoleService Registered with Notification Manager")
        }
        val emailProperties = getEmailProperties()

        if (emailProperties != null) {
            activeSenders.add(EmailManager(emailProperties))
            logger.debug("EmailService Registered with Notification Manager")
        }
    }

    private fun getEmailProperties(): EmailNotifications? {

        if (!(config.property("herocraft.notifications.email.enabled").getString().toBoolean() ?: false)) {
            return null
        }

        val email = config.property("herocraft.notifications.email.sender.email").getString()
        val name = config.property("herocraft.notifications.email.sender.name").getString()
        val username = config.property("herocraft.notifications.email.sender.username").getString()
        val password = config.property("herocraft.notifications.email.sender.password").getString()

        if (email.isBlank() or name.isBlank()) {
            logger.error("Missing Sender Info, Email Notifications Disabled")
            return null
        }

        val emailSender = EmailSender(email, name, username, password)

        val startTlSRequired = config.property("herocraft.notifications.email.security.StartTLSRequired").getString().toBoolean() ?: false
        val SSLOnConnect = config.property("herocraft.notifications.email.security.SSLOnConnect").getString().toBoolean() ?: false

        val emailSecurity = EmailSecurity(startTlSRequired, SSLOnConnect)

        val port = config.property("herocraft.notifications.email.smtpPort").getString().toInt() ?: 465
        val host = config.property("herocraft.notifications.email.hostname").getString()

        if (host.isBlank()) {
            logger.error("Host name Missing, Email Notifications Disabled")
            return null
        }

        return EmailNotifications(true, hostName = host, port = port, sender = emailSender, security = emailSecurity)
    }

    fun sendEmailVerification(to: String, token: String) {
        activeSenders.forEach {
            logger.debug("Logging Verification Email for ${it::class.qualifiedName} ")
            it.sendVerificationEmail(to, token)
        }
    }

    fun sendPasswordReset(to: String, username: String, token: String) {
        activeSenders.forEach {
            logger.debug("Logging Password Reset for ${it::class.qualifiedName} ")
            it.sendPasswordReset(to, username, token)
        }
    }

}