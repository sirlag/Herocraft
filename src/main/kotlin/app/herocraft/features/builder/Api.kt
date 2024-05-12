package app.herocraft.features.builder

import app.herocraft.core.models.DeckFormat
import app.herocraft.core.models.DeckVisibility
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class DeckRequest(
    val name: String,
    val format: DeckFormat,
    val visibility: DeckVisibility
)

@Serializable
data class DeckSettings(
    val name: String,
    val format: DeckFormat,
    val visibility: DeckVisibility
)

@Serializable
data class DeckImportRequest(
    val decks: List<DeckImportRequestDeck>,
    val defaultVisibility: DeckVisibility,
)

@Serializable
data class DeckImportRequestDeck(
    val name: String,
    val list: List<DeckImportRequestCard>,
    val traits: List<String>,
    val specialization: String
)

@Serializable
data class DeckImportRequestCard (
    val name: String,
    val uuid: UUID,
    val count: Int
)

@Serializable
data class DeckEditRequest(
    val cardId: UUID,
    val count: Int
)