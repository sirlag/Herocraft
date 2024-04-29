package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class IvionDeck(
    val id: UUID,
    val hash: String,
    val name: String,
    val list: MutableList<IvionDeckEntry>,
    val owner: UUID,
    val visibility: DeckVisibility,
    val format: DeckFormat,
    var ownerName: String? = null,
)


@Serializable
data class IvionDeckEntry(
    val count: Int,
    val card: IvionCard
)

@Serializable
enum class DeckVisibility {
    PUBLIC,
    UNLISTED,
    PRIVATE
}

@Serializable
enum class DeckFormat {
    CONSTRUCTED,
    PARAGON,
    OTHER
}