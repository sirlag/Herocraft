package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlin.random.Random

@Serializable
data class IvionCard(
    val id: UUID = UUID.generateUUID(Random),
    val collectorsNumber: Int?,
    val format: String?,
    val name: String,
    val archetype: String?,
    val actionCost: Int?,
    val powerCost: Int?,
    val range: Int?,
    val health: Int?,
    val heroic: Boolean =false,
    val slow: Boolean = false,
    val silence: Boolean = false,
    val disarm: Boolean = false,
    val extraType: String?,
    val rulesText: String?,
    val flavorText: String?,
    val artist: String,
    val ivionUUID: UUID,
    val colorPip1: String?,
    val colorPip2: String?,
    val season: String,
    val type: String?
)

//sealed class
//
//enum class AffectedControl{
//    HERORIC,
//    CONTROLLED(
//        val slow: Boolean,
//        val Silence: Boolean,
//        val Disarm: Boolean
//    )
//}