package app.herocraft.features.notifications

import org.slf4j.LoggerFactory

class ConsoleService : NotificationSender {

    val logger = LoggerFactory.getLogger(ConsoleService::class.java)!!

    override fun sendVerificationEmail(to: String, token: String) {
        logger.warn("Verification URL for $to is: https://herocraft.app/account/verify/$token")
    }

    override fun sendPasswordReset(to: String, username: String, token: String) {
        logger.warn("Password reset for $to is: https://herocraft.app/account/reset/$token")
    }


}