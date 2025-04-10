package app.herocraft.core.models

import kotlinx.serialization.Serializable

import kotlin.uuid.Uuid

@Serializable
data class IvionCard(
    val id: Uuid = Uuid.random(),
    val collectorsNumber: String?,
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
    val ivionUUID: Uuid,
    val secondUUID: Uuid?,
    val colorPip1: String?,
    val colorPip2: String?,
    val season: String,
    val type: String?
) {
    fun isUltimate() = type == "Ultimate"
}

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