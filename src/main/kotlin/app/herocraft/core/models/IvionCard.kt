package app.herocraft.core.models

import app.herocraft.features.images.ImageProcessor
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

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
    val type: String?,
    // Physical layout akin to Scryfall's layout. Optional for backward compatibility
    val layout: CardLayout? = null,
    val imageUris: IvionCardImageURIs? = null,
    // New optional fields for advanced card modeling (multi-face, links, variants)
    val faces: List<IvionCardFaceData>? = null,
    val linkedParts: List<LinkedPart> = emptyList(),
    val herocraftId: Uuid? = null,
    val printVariantGroupId: Uuid? = null,
    val variants: List<Uuid> = emptyList(),
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

@Serializable
data class IvionCardImageURIs(
    val full: String,
    val large: String,
    val normal: String,
    val small: String,
)


@Serializable
data class IvionCardFaceData(
    val face: CardFace,
    val name: String? = null,
    val rulesText: String? = null,
    val flavorText: String? = null,
    val artist: String? = null,
    val imageUris: IvionCardImageURIs? = null,
)


@Serializable
data class LinkedPart(
    val id: Uuid,
    val relation: LinkedRelation,
)

@Serializable
enum class LinkedRelation {
    TOKEN,
    GENERATED_BY,
    TRANSFORMS_FROM,
    TRANSFORMS_INTO,
}

@Serializable
enum class CardLayout {
    NORMAL,
    TRANSFORM,
    TOKEN,
}


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