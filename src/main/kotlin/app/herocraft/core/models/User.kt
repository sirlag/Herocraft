package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class User(
    val id: UUID,
    val username: String,
    val email: String,
    val verified: Boolean,
)