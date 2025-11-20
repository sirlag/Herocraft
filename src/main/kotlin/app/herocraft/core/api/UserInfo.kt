package app.herocraft.core.api

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserInfo(
    val id: Uuid,
    val username: String,
    val displayName: String,
    val email: String,
    val verified: Boolean,
)