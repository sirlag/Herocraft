package app.herocraft.core.security

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)