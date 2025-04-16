package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.extensions.isEmailAddress
import app.herocraft.features.notifications.NotificationManager
import arrow.core.Either
import io.ktor.server.plugins.*
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

data class UserNotFoundException(val key: String): RuntimeException()
data class UserNotVerifiedExeception(val key: String): RuntimeException()

class UserService(
    private val userRepo: UserRepo,
    private val resetTokenRepo: ResetTokenRepo,
    private val notificationManager: NotificationManager,
    private val verificationRepo: VerificationRepo,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    suspend fun registerUser(userRequest: UserRequest): Uuid? {

        val userId = userRepo.create(userRequest)

        if (userId != null) {
            val token = verificationRepo.create(userId)
            notificationManager.sendEmailVerification(userRequest.email, token)
            return userId
        } else {
            return null
        }
    }

    suspend fun resetPassword(request: PasswordResetRequest): Boolean {

        if (request.password.isNullOrBlank() || request.token.isNullOrBlank()) {
            return false
        }

        logger.debug("Reset Password Request: Token is ${request.token}")

        val userId = resetTokenRepo.consumeToken(token = request.token) ?: return false
        val changedPass = userRepo.changePassword(userId, request.password)
        return changedPass > 0
    }

    suspend fun sendForgotPasswordEmail(email: String): Result<Uuid> = runCatching{
        require(email.isEmailAddress())

        val user = userRepo.getUserWithEmail(email) ?: throw UserNotFoundException("email = $email")
        if (!user.verified) throw UserNotVerifiedExeception("")
        val token = resetTokenRepo.create(user.id)

        notificationManager.sendPasswordReset(user.email, user.username, token)
        user.id
    }

    suspend fun sendEmailVerificationEmail(id: Uuid, email: String) {
        val token = verificationRepo.create(id)
        notificationManager.sendEmailVerification(email, token)
    }

    suspend fun verifyEmail(token: String): Boolean {
        return verificationRepo.verify(token)
    }
}