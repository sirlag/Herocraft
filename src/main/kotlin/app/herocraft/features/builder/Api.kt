package app.herocraft.features.builder

import app.herocraft.core.models.DeckFormat
import app.herocraft.core.models.DeckVisibility
import kotlinx.serialization.Serializable

@Serializable
data class DeckRequest(
    val name: String,
    val format: DeckFormat,
    val visibility: DeckVisibility
)