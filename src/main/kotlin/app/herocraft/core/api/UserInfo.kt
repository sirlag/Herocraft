package app.herocraft.core.api

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class UserInfo(
    val id: UUID,
    val username: String,
    val email: String,
    val verified: Boolean,
)