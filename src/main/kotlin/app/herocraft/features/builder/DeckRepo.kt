package app.herocraft.features.builder

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.*
import app.herocraft.core.security.UserRepo
import app.herocraft.features.search.CardRepo
import app.herocraft.features.search.CardRepo.Card
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid


@Serializable
data class CreateDeckResponse(val uuid: Uuid, val hash: String)

@Serializable
data class EditDeckResponse(val cardId: Uuid, val count: Int, val changeSpec: Boolean, val spec: String?)

class DeckRepo(
    database: Database,
    private val userRepo: UserRepo,
    private val cardRepo: CardRepo
): DataService(database) {
    object Deck : Table() {
        val id = uuid("id")
        val hash = text("hash")
        val name = text("name")
        val primarySpec = text("primary_spec").nullable()
        val owner = uuid("owner") //Make this a foreign key constraint
        val visibility = enumeration("visibility", DeckVisibility::class)
        val format = enumeration("format", DeckFormat::class)
        val lastModified = timestamp("last_modified")
        val created = timestamp("created")

        override val primaryKey = PrimaryKey(id)
    }

    object DeckEntry : Table() {
        val deckId = uuid("deck_id")
        val cardId = uuid("card_id")
        val count = integer("count")

        override val primaryKey = PrimaryKey(deckId, cardId)
    }

//    object DeckHistory : Table() {
//        val id = uuid("deck_history_id")
//        val deckId = uuid("deck_id")
//        val cardId = uuid("card_id")
//        val change = integer("change")
//        val timestamp = timestamp("timestamp")
//    }

    suspend fun getPaging(size: Int = 60, page: Int = 1) = paged(size, page, { Deck.id.count()}, { Op.TRUE})

    private suspend fun paged(
        size: Int  = 60,
        page: Int = 1,
        total: () -> Count = { Deck.id.count()},
        query: (SqlExpressionBuilder.() -> Op<Boolean>))
            = dbQuery {
        val totalCount = total().alias("total_count")
        val count = Deck
            .select(totalCount)
            .where(query)
            .map { it[totalCount] }
            .first()

        val offset = (page-1L)*size
        val cards = Deck
            .selectAll()
            .where(query)
            .orderBy(Deck.name, SortOrder.ASC)
            .limit(size)
            .offset(offset)
            .map {
                Deck.fromResultRow(it)
            }.toList()
        Page(
            items = cards,
            itemCount = cards.size.toLong(),
            totalItems = count,
            page = page,
            pageSize = size,
            totalPages = ((count/size)+1).toInt()
        )
    }

    suspend fun createDeck(
        userId: Uuid,
        name: String,
        format: DeckFormat,
        visibility: DeckVisibility,
        spec: String? = null
    ): CreateDeckResponse {

        val id = Uuid.random()
        val hash = id.toByteArray().encodeBase64()
            .replace("/", "_")
            .replace("+", "-")
            .substring(0, 22)

        val now = Clock.System.now()

        dbQuery {
            Deck.selectAll().where(Deck.id.eq(id.toJavaUuid()))
            Deck.insert {
                it[Deck.id] = id.toJavaUuid()
                it[Deck.hash] = hash
                it[Deck.name] = name
                it[primarySpec] = spec
                it[owner] = userId.toJavaUuid()
                it[Deck.visibility] = visibility
                it[Deck.format] = format
                it[created] = now
                it[lastModified] = now
            }
        }

        return CreateDeckResponse(id, hash);
    }

    suspend fun importAll(userId: Uuid, deckImportRequest: DeckImportRequest): List<CreateDeckResponse>  {

        val newDeckIds = mutableListOf<CreateDeckResponse>()
        for (deck: DeckImportRequestDeck in deckImportRequest.decks) {
            val deckIds = createDeck(
                userId,
                deck.name,
                DeckFormat.CONSTRUCTED,
                deckImportRequest.defaultVisibility,
                deck.specialization)
            dbQuery {
                for (card: DeckImportRequestCard in deck.list) {
                    DeckEntry.insert {
                        it[deckId] = deckIds.uuid.toJavaUuid()
                        it[cardId] = CardRepo.Card
                            .select(CardRepo.Card.id)
                            .where(CardRepo.Card.name eq card.name)
                        it[count] = card.count
                    }
                }
                for (name: String in deck.traits) {
                    DeckEntry.insert {
                        it[deckId] = deckIds.uuid.toJavaUuid()
                        it[cardId] = CardRepo.Card
                            .select(CardRepo.Card.id)
                            .where(CardRepo.Card.name eq name)
                        it[count] = 1
                    }
                }
                DeckEntry.insert {
                    it[deckId] = deckIds.uuid.toJavaUuid()
                    it[cardId] = CardRepo.Card
                        .select(CardRepo.Card.id)
                        .where(CardRepo.Card.archetype eq deck.specialization and (CardRepo.Card.type eq "Ultimate"))
                    it[count] = 1
                }
                newDeckIds.add(deckIds)
            }
        }
        return newDeckIds

    }

    suspend fun getUserDecks(userId: Uuid): List<IvionDeck> = dbQuery {
        Deck
            .selectAll()
            .where { Deck.owner eq (userId.toJavaUuid()) }
            .map { Deck.fromResultRow(it) }
            .toList()
    }

    suspend fun getDeckList(deckHash: String): IvionDeck = dbQuery {
        val deck = Deck
            .selectAll()
            .where { Deck.hash eq (deckHash)}
            .map { Deck.fromResultRow(it) }
            .first()
        val name = userRepo.getUsername(deck.owner)
        deck.ownerName = name
        val deckList = getDeckList(deck.id)
        deck.list.addAll(deckList)
        return@dbQuery deck
    }

    suspend fun updateSettings(userId: Uuid, deckId: Uuid, settings: DeckSettings) = dbQuery {
        val now = Clock.System.now()

        val updatedDeck = Deck
            .updateReturning(
                where = { Deck.id eq deckId.toJavaUuid() and (Deck.owner eq userId.toJavaUuid()) }
            ) {
                it[name] = settings.name
                it[visibility] = settings.visibility
                it[format] = settings.format
                it[lastModified] = now
            }
            .map { Deck.fromResultRow(it) }
            .first()
        return@dbQuery updatedDeck
    }

    suspend fun deleteDeck(userId: Uuid, deckId: Uuid) = dbQuery {
        return@dbQuery Deck
            .deleteWhere { owner eq userId.toJavaUuid() and (id eq deckId.toJavaUuid())}
    }

    private fun Deck.fromResultRow(result: ResultRow): IvionDeck =
        IvionDeck(
            result[id].toKotlinUuid(),
            result[hash],
            result[name],
            mutableListOf(),
            result[primarySpec],
            result[owner].toKotlinUuid(),
            result[visibility],
            result[format],
            result[created],
            result[lastModified]
        )

    // DECK ENTRY STUFF

    suspend fun getDeckList(deckId: Uuid) = dbQuery {
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
            .where{ DeckEntry.deckId eq deckId.toJavaUuid() }
            .map { it.toIvionDeckEntry() }
            .toList()

    }

    suspend fun editDeck(userId: Uuid, deckHash: String, cardId: Uuid, count: Int): EditDeckResponse? = dbQuery {
        val deck = Deck.selectAll()
            .where{ Deck.owner eq userId.toJavaUuid() and (Deck.hash eq deckHash)}
            .limit(1)
            .map { Deck.fromResultRow(it) }
            .firstOrNull()
        val card = cardRepo.getOne(cardId)

        if (deck == null || card == null) {
            return@dbQuery null
        }

        val deckUUID = deck.id.toJavaUuid()
        val now = Clock.System.now()

        if (count > 0) {
            DeckEntry.upsert {
                it[deckId] = deckUUID
                it[DeckEntry.cardId] = cardId.toJavaUuid()
                it[DeckEntry.count] = count
            }
        } else {
            DeckEntry.deleteWhere { deckId eq deckUUID and (DeckEntry.cardId eq cardId.toJavaUuid())}
        }

//        println((card.toString()))
//        println

        var changeSpec: Boolean = false
        var spec: String? = null

        Deck.update(
            where = {Deck.id eq deckUUID}
        ) {
            it[lastModified] = now
            if (card.isUltimate()) {
                if (deck.primarySpec == null && count > 0) {
                    it[primarySpec] = card.archetype
                    changeSpec = true
                    spec = card.archetype

                } else if (deck.primarySpec == card.archetype && count <= 0) {
                    it[primarySpec] = null
                    changeSpec = true
                    spec = null;
                }
            }
        }

        return@dbQuery EditDeckResponse(cardId, count, changeSpec, spec)
    }

    private fun ResultRow.toIvionDeckEntry() : IvionDeckEntry =
        IvionDeckEntry(
            count = this[DeckEntry.count],
            card = IvionCard(
                this[DeckEntry.cardId].toKotlinUuid(),
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
                this[Card.ivionUUID].toKotlinUuid(),
                this[Card.secondUUID]?.toKotlinUuid(),
                this[Card.colorPip1],
                this[Card.colorPip2],
                this[Card.season],
                this[Card.type]
            )
        )



}