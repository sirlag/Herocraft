package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid,
    val username: String,
    val displayName: String,
    val email: String,
    val verified: Boolean,
    // Derived flag: true if the user has any role granting admin permission
    val isAdmin: Boolean = false,
)