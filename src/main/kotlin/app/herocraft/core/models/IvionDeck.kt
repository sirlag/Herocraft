package app.herocraft.core.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class IvionDeck(
    val id: Uuid,
    val hash: String,
    val name: String,
    val list: MutableList<IvionDeckEntry>,
    val primarySpec: String?,
    val owner: Uuid,
    val visibility: DeckVisibility,
    val format: DeckFormat,
    val created: Instant,
    val lastModified: Instant,
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