package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid,
    val username: String,
    val email: String,
    val verified: Boolean,
)