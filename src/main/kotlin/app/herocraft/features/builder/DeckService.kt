package app.herocraft.features.builder

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.*
import app.herocraft.core.security.UserService
import app.herocraft.features.search.CardService
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.uuid.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import kotlin.random.Random


@Serializable
data class CreateDeckResponse(val uuid: UUID, val hash: String)

class DeckService(
    database: Database,
    private val deckListService: DeckListService,
    private val userService: UserService
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
            .limit(size, offset)
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
        userId: UUID,
        name: String,
        format: DeckFormat,
        visibility: DeckVisibility,
        spec: String? = null
    ): CreateDeckResponse {

        val id = UUID.generateUUID(Random)
        val hash = id.encodeToByteArray().encodeBase64()
            .replace("/", "_")
            .replace("+", "-")
            .substring(0, 22)

        val now = Clock.System.now()

        dbQuery {
            Deck.selectAll().where(Deck.id.eq(id.toJavaUUID()))
            Deck.insert {
                it[Deck.id] = id.toJavaUUID()
                it[Deck.hash] = hash
                it[Deck.name] = name
                it[primarySpec] = spec
                it[owner] = userId.toJavaUUID()
                it[Deck.visibility] = visibility
                it[Deck.format] = format
                it[created] = now
                it[lastModified] = now
            }
        }

        return CreateDeckResponse(id, hash);
    }

    suspend fun importAll(userId: UUID, deckImportRequest: DeckImportRequest): List<CreateDeckResponse>  {

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
                    DeckListService.DeckEntry.insert {
                        it[deckId] = deckIds.uuid.toJavaUUID()
                        it[cardId] = CardService.Card
                            .select(CardService.Card.id)
                            .where(CardService.Card.name eq card.name)
                        it[count] = card.count
                    }
                }
                for (name: String in deck.traits) {
                    DeckListService.DeckEntry.insert {
                        it[deckId] = deckIds.uuid.toJavaUUID()
                        it[cardId] = CardService.Card
                            .select(CardService.Card.id)
                            .where(CardService.Card.name eq name)
                        it[count] = 1
                    }
                }
                DeckListService.DeckEntry.insert {
                    it[deckId] = deckIds.uuid.toJavaUUID()
                    it[cardId] = CardService.Card
                        .select(CardService.Card.id)
                        .where(CardService.Card.archetype eq deck.specialization and (CardService.Card.type eq "Ultimate"))
                    it[count] = 1
                }
                newDeckIds.add(deckIds)
            }
        }
        return newDeckIds

    }

    suspend fun getUserDecks(userId: UUID): List<IvionDeck> = dbQuery {
        Deck
            .selectAll()
            .where { Deck.owner eq (userId.toJavaUUID()) }
            .map { Deck.fromResultRow(it) }
            .toList()
    }

    suspend fun getDeck(deckHash: String): IvionDeck = dbQuery {
        val deck = Deck
            .selectAll()
            .where { Deck.hash eq (deckHash)}
            .map { Deck.fromResultRow(it) }
            .first()
        val name = userService.getUsername(deck.owner)
        deck.ownerName = name
        val deckList = deckListService.getDeck(deck.id)
        deck.list.addAll(deckList)
        return@dbQuery deck
    }

    suspend fun updateSettings(userId: UUID, deckId: UUID, settings: DeckSettings) = dbQuery {
        val now = Clock.System.now()

        val updatedDeck = Deck
            .updateReturning(
                where = { Deck.id eq deckId.toJavaUUID() and (Deck.owner eq userId.toJavaUUID()) }
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

    suspend fun deleteDeck(userId: UUID, deckId: UUID) = dbQuery {
        return@dbQuery Deck
            .deleteWhere { owner eq userId.toJavaUUID() and (id eq deckId.toJavaUUID())}
    }

    private fun Deck.fromResultRow(result: ResultRow): IvionDeck =
        IvionDeck(
            result[id].toKotlinUUID(),
            result[hash],
            result[name],
            mutableListOf(),
            result[primarySpec],
            result[owner].toKotlinUUID(),
            result[visibility],
            result[format],
            result[created],
            result[lastModified]
        )
}