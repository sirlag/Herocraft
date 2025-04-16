package app.herocraft.core.models

import app.herocraft.features.images.ImageProcessor
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

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


//    object CardImage: Table("cardimages") {
//        val id = uuid("image_id")
//        val card_id = reference(
//            name = "card_id",
//            refColumn = Card.id,
//            onDelete = ReferenceOption.CASCADE)
//        val variant = text("variant")
//        val face = varchar("face", 5)
//        val uri = text("uri")
//        val mimeType = varchar(name = "mime_type", length = 100)
//        val byteSize = integer("byte_size")
//        val createdAt = datetime("created_at")
//        val updatedAt = datetime("updated_at")
//    }

data class IvionCardImage(
    val id: Uuid = Uuid.random(),
    val cardId: Uuid,
    val variant: ImageProcessor.ImageSize,
    val face: CardFace,
    val uri: String,
    val mimeType: String,
    val byteSize: Int,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
)

enum class CardFace {
    FRONT,
    BACK;

    override fun toString(): String {
        return when (this) {
            FRONT -> "front"
            BACK -> "back"
        }
    }
}