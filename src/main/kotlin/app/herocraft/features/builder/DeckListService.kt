package app.herocraft.features.builder

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.IvionDeckEntry
import app.herocraft.features.search.CardService.Card
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table


class DeckListService(database: Database) : DataService(database) {
    object DeckEntry : Table() {
        val deckId = uuid("deck_id")
        val cardId = uuid("card_id")
        val count = integer("count")

        override val primaryKey = PrimaryKey(deckId, cardId)
    }

    suspend fun getDeck(deckId: UUID) = dbQuery {
        return@dbQuery DeckEntry
            .join(Card, JoinType.INNER, DeckEntry.cardId, Card.id)
            .select(
                DeckEntry.deckId,
                DeckEntry.cardId,
                DeckEntry.count,
                Card.name,
                Card.format,
                Card.collectorsNumber,
                Card.format,
                Card.name,
                Card.archetype,
                Card.actionCost,
                Card.powerCost,
                Card.range,
                Card.health,
                Card.heroic,
                Card.slow,
                Card.silence,
                Card.disarm,
                Card.extraType,
                Card.rulesText,
                Card.flavorText,
                Card.artist,
                Card.ivionUUID,
                Card.secondUUID,
                Card.colorPip1,
                Card.colorPip2,
                Card.season,
                Card.type,
            )
            .where{ DeckEntry.deckId eq deckId.toJavaUUID() }
            .map { it.toIvionDeckEntry() }
            .toList()

    }

    private fun ResultRow.toIvionDeckEntry() : IvionDeckEntry =
        IvionDeckEntry(
            count = this[DeckEntry.count],
            card = IvionCard(
                this[DeckEntry.cardId].toKotlinUUID(),
                this[Card.collectorsNumber],
                this[Card.format],
                this[Card.name],
                this[Card.archetype],
                this[Card.actionCost],
                this[Card.powerCost],
                this[Card.range],
                this[Card.health],
                this[Card.heroic],
                this[Card.slow],
                this[Card.silence],
                this[Card.disarm],
                this[Card.extraType],
                this[Card.rulesText],
                this[Card.flavorText],
                this[Card.artist],
                this[Card.ivionUUID].toKotlinUUID(),
                this[Card.secondUUID]?.toKotlinUUID(),
                this[Card.colorPip1],
                this[Card.colorPip2],
                this[Card.season],
                this[Card.type]
            )
        )

}