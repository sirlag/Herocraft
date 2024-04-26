package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class IvionDeck(
    val id: UUID,
    val hash: String,
    val name: String,
    val list: List<IvionCard>,
    val owner: UUID,
    val visibility: DeckVisibility
)


enum class DeckVisibility {
    PUBLIC,
    UNLISTED,
    PRIVATE
}