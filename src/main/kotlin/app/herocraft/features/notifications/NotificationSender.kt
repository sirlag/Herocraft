package app.herocraft.features.notifications

interface NotificationSender {
    fun sendVerificationEmail(to: String, token: String)
    fun sendPasswordReset(to: String, username: String, token: String)
}