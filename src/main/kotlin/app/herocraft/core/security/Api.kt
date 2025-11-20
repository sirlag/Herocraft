package app.herocraft.core.security

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegistrationRequest(val username:String, val email: String, val password: String)

@Serializable
data class RequestPasswordResetRequest(val email: String)

@Serializable
data class PasswordResetRequest(val password: String?, val token: String?)

@Serializable
data class ProfileUpdateRequest(val displayName: String)

@Serializable
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)