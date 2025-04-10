package app.herocraft.core.security

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegistrationRequest(val username:String, val email: String, val password: String)

@Serializable
data class ResetPasswordRequest(val email: String)