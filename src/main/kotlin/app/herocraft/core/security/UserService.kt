package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.extensions.isEmailAddress
import app.herocraft.features.notifications.NotificationManager
import kotlin.uuid.Uuid

class UserService(
    private val userRepo: UserRepo,
    private val resetTokenRepo: ResetTokenRepo,
    private val notificationManager: NotificationManager,
    private val verificationRepo: VerificationRepo,
) {

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

    suspend fun sendForgotPasswordEmail(email: String): Boolean {
        if (!email.isEmailAddress()) {
            return false
        }

        val user = userRepo.getUserWithEmail(email) ?: return false
        if (!user.verified) {
            return false
        }
        val token = resetTokenRepo.create(user.id)

        notificationManager.sendPasswordReset(user.email, user.username, token)
        return true
    }

    suspend fun sendEmailVerificationEmail(id: Uuid, email: String) {
        val token = verificationRepo.create(id)
        notificationManager.sendEmailVerification(email, token)
    }

    suspend fun verifyEmail(token: String): Boolean {
        return verificationRepo.verify(token)
    }
}