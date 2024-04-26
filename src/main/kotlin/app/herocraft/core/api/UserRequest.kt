package app.herocraft.core.api

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(val username: String, val email: String, val password: String)