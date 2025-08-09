package app.herocraft.features.builder

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.DeckFacts
import app.herocraft.core.models.DeckFormat
import app.herocraft.core.models.DeckVisibility
import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.IvionDeck
import app.herocraft.core.models.IvionDeckEntry
import app.herocraft.core.models.Page
import app.herocraft.core.security.UserRepo
import app.herocraft.features.search.CardRepo
import app.herocraft.features.search.CardRepo.Card
import io.ktor.util.*
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Count
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.updateReturning
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID.randomUUID
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
) : DataService(database) {
    object Deck : UUIDTable("deck", columnName = "id") {
        val hash = text("hash")
        val name = text("name")
        val primarySpec = text("primary_spec").nullable()
        val owner = uuid("owner") //Make this a foreign key constraint
        val visibility = enumeration("visibility", DeckVisibility::class)
        val format = enumeration("format", DeckFormat::class)
        val lastModified = timestamp("last_modified")
        val created = timestamp("created")

    }

    object DeckEntry : Table("deckentry") {
        val deckId = uuid("deck_id")
        val cardId = uuid("card_id")
        val count = integer("count")

        override val primaryKey = PrimaryKey(deckId, cardId)
    }

    object DeckLikes : Table("deck_likes") {
        val id = uuid("id").uniqueIndex()
        val deckId = reference("deck_id", Deck.id)
        val userId = uuid("user_id")
        val timestamp = timestamp("timestamp")
        val deleted = bool("deleted").nullable()
        val deletedAt = timestamp("deleted_at").nullable()

        override val primaryKey = PrimaryKey(id)
        val unique = uniqueIndex(customIndexName = "unq_deck_likes_deck_user", deckId, userId)
    }

    object DeckFavorites : Table("deck_favorites") {
        var id = uuid("id").uniqueIndex()
        val deckId = reference("deck_id", Deck.id)
        val userId = uuid("user_id")
        val timestamp = timestamp("timestamp")
        val deleted = bool("deleted").nullable()
        val deletedAt = timestamp("deleted_at").nullable()

        init {
            foreignKey(deckId to Deck.id)
        }

    }

    object DeckFactsAggregate : Table("deck_facts_aggregate") {
        val id = uuid("id")
        val deckId = reference("deck_id", Deck.id)
        val views = integer("views")
        val likes = integer("likes")

        init {
            foreignKey(deckId to Deck.id)
        }


        fun toDeckFacts(results: ResultRow): DeckFacts =
            DeckFacts(
                results[id].toKotlinUuid(),
                results[deckId].value.toKotlinUuid(),
                results[likes],
                results[views],
            )

    }


//    class DeckEntity(id: EntityID<UUID>) : UUIDEntity(id) {
//        companion object : UUIDEntityClass<DeckEntity>(Deck)
//
//        val hash by Deck.hash
//        val name by Deck.name
//        val primarySpec by Deck.primarySpec
//        val owner by Deck.primarySpec
//        val visibility by Deck.visibility
//        val format by Deck.format
//        val lastModified by Deck.lastModified
//        val created by Deck.created
//
//
//
//    }

//    object DeckHistory : Table() {
//        val id = uuid("deck_history_id")
//        val deckId = uuid("deck_id")
//        val cardId = uuid("card_id")
//        val change = integer("change")
//        val timestamp = timestamp("timestamp")
//    }

    suspend fun getPaging(size: Int = 60, page: Int = 1) = paged(size, page, { Deck.id.count() }, { Op.TRUE })

    private suspend fun paged(
        size: Int = 60,
        page: Int = 1,
        total: () -> Count = { Deck.id.count() },
        query: (SqlExpressionBuilder.() -> Op<Boolean>)
    ) = dbQuery {
        val totalCount = total().alias("total_count")
        val count = Deck
            .select(totalCount)
            .where(query)
            .map { it[totalCount] }
            .first()

        val offset = (page - 1L) * size
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
            totalPages = ((count / size) + 1).toInt()
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

        return CreateDeckResponse(id, hash)
    }

    suspend fun importAll(userId: Uuid, deckImportRequest: DeckImportRequest): List<CreateDeckResponse> {

        val newDeckIds = mutableListOf<CreateDeckResponse>()
        for (deck: DeckImportRequestDeck in deckImportRequest.decks) {
            val deckIds = createDeck(
                userId,
                deck.name,
                DeckFormat.CONSTRUCTED,
                deckImportRequest.defaultVisibility,
                deck.specialization
            )
            dbQuery {
                for (card: DeckImportRequestCard in deck.list) {
                    DeckEntry.insert {
                        it[deckId] = deckIds.uuid.toJavaUuid()
                        it[cardId] = Card
                            .select(Card.id)
                            .where(Card.name eq card.name)
                        it[count] = card.count
                    }
                }
                for (name: String in deck.traits) {
                    DeckEntry.insert {
                        it[deckId] = deckIds.uuid.toJavaUuid()
                        it[cardId] = Card
                            .select(Card.id)
                            .where(Card.name eq name)
                        it[count] = 1
                    }
                }
                DeckEntry.insert {
                    it[deckId] = deckIds.uuid.toJavaUuid()
                    it[cardId] = Card
                        .select(Card.id)
                        .where(Card.archetype eq deck.specialization and (Card.type eq "Ultimate"))
                    it[count] = 1
                }
                newDeckIds.add(deckIds)
            }
        }
        return newDeckIds

    }

    fun Deck.selectFields() = this.join(
        otherTable = DeckFavorites,
        joinType = JoinType.LEFT,
        onColumn = Deck.id,
        otherColumn = DeckFavorites.deckId
    )
        .join(
            otherTable = DeckFactsAggregate,
            joinType = JoinType.LEFT,
            onColumn = Deck.id,
            otherColumn = DeckFactsAggregate.deckId
        )
        .select(
            listOf(
                Deck.id,
                Deck.hash,
                Deck.name,
                Deck.name,
                Deck.primarySpec,
                Deck.owner,
                Deck.visibility,
                Deck.format,
                Deck.lastModified,
                Deck.created,
                DeckFactsAggregate.views,
                DeckFactsAggregate.likes,
                DeckFavorites.deleted,
            )
        )

    suspend fun getUserDecks(userId: Uuid): List<IvionDeck> = dbQuery {
        Deck
            .selectFields()
            .where { Deck.owner eq (userId.toJavaUuid()) }
            .map { Deck.fromResultRow(it) }
            .toList()
    }

    suspend fun getUserDecksPaginated(
        userId: Uuid,
        size: Int = 20,
        page: Int = 1
    ): Page<IvionDeck> = dbQuery {
        // Get total count of user's decks
        val totalCount = Deck
            .select(Deck.id.count())
            .where { Deck.owner eq userId.toJavaUuid() }
            .map { it[Deck.id.count()] }
            .first()

        // Calculate offset
        val offset = (page - 1L) * size

        // Get paginated decks
        val decks = Deck
            .join(UserRepo.Users, JoinType.INNER, Deck.owner, UserRepo.Users.id)
            .join(DeckFavorites, JoinType.LEFT, Deck.id, DeckFavorites.deckId)
            .join(DeckFactsAggregate, JoinType.LEFT, Deck.id, DeckFactsAggregate.deckId)
            .select(
                Deck.id,
                Deck.hash,
                Deck.name,
                Deck.primarySpec,
                Deck.owner,
                Deck.visibility,
                Deck.format,
                Deck.created,
                Deck.lastModified,
                DeckFactsAggregate.views,
                DeckFactsAggregate.likes,
                DeckFavorites.deleted,
                UserRepo.Users.username
            )
            .where { Deck.owner eq userId.toJavaUuid() }
            .orderBy(Deck.lastModified, SortOrder.DESC)
            .limit(size)
            .offset(offset)
            .map { row ->
                val deck = Deck.fromResultRow(row)
                deck.ownerName = row[UserRepo.Users.username]
                deck
            }
            .toList()

        return@dbQuery Page(
            items = decks,
            itemCount = decks.size.toLong(),
            totalItems = totalCount,
            page = page,
            pageSize = size,
            totalPages = ((totalCount + size - 1) / size).toInt()
        )
    }

    suspend fun getUserLikedDecks(userId: Uuid): List<IvionDeck> = dbQuery {
        Deck
            .join(DeckLikes, JoinType.INNER, Deck.id, DeckLikes.deckId)
            .join(UserRepo.Users, JoinType.INNER, Deck.owner, UserRepo.Users.id)
            .select(
                Deck.id,
                Deck.hash,
                Deck.name,
                Deck.primarySpec,
                Deck.owner,
                Deck.visibility,
                Deck.format,
                Deck.created,
                Deck.lastModified,
                UserRepo.Users.username
            )
            .where { 
                (DeckLikes.userId eq userId.toJavaUuid()) and 
                (DeckLikes.deleted neq true) and
                (Deck.visibility eq DeckVisibility.PUBLIC)
            }
            .orderBy(DeckLikes.timestamp, SortOrder.DESC)
            .map { row ->
                val deck = Deck.fromResultRow(row)
                deck.ownerName = row[UserRepo.Users.username]
                deck
            }
            .toList()
    }

    suspend fun getUserLikedDecksPaginated(
        userId: Uuid,
        size: Int = 20,
        page: Int = 1
    ): Page<IvionDeck> = dbQuery {
        // Get total count of user's liked decks
        val totalCount = Deck
            .join(DeckLikes, JoinType.INNER, Deck.id, DeckLikes.deckId)
            .select(Deck.id.count())
            .where { 
                (DeckLikes.userId eq userId.toJavaUuid()) and 
                (DeckLikes.deleted neq true) and
                (Deck.visibility eq DeckVisibility.PUBLIC)
            }
            .map { it[Deck.id.count()] }
            .first()

        // Calculate offset
        val offset = (page - 1L) * size

        // Get paginated liked decks
        val decks = Deck
            .join(DeckLikes, JoinType.INNER, Deck.id, DeckLikes.deckId)
            .join(UserRepo.Users, JoinType.INNER, Deck.owner, UserRepo.Users.id)
            .select(
                Deck.id,
                Deck.hash,
                Deck.name,
                Deck.primarySpec,
                Deck.owner,
                Deck.visibility,
                Deck.format,
                Deck.created,
                Deck.lastModified,
                UserRepo.Users.username
            )
            .where { 
                (DeckLikes.userId eq userId.toJavaUuid()) and 
                (DeckLikes.deleted neq true) and
                (Deck.visibility eq DeckVisibility.PUBLIC)
            }
            .orderBy(DeckLikes.timestamp, SortOrder.DESC)
            .limit(size)
            .offset(offset)
            .map { row ->
                val deck = Deck.fromResultRow(row)
                deck.ownerName = row[UserRepo.Users.username]
                deck
            }
            .toList()

        return@dbQuery Page(
            items = decks,
            itemCount = decks.size.toLong(),
            totalItems = totalCount,
            page = page,
            pageSize = size,
            totalPages = ((totalCount + size - 1) / size).toInt()
        )
    }

    suspend fun getRecentPublicDecksPaginated(
        size: Int = 20,
        page: Int = 1
    ): Page<IvionDeck> = dbQuery {
        // Gets total count of public decks
        val totalCount = Deck
            .select(Deck.id.count())
            .where { Deck.visibility eq DeckVisibility.PUBLIC }
            .map { it[Deck.id.count()] }
            .first()

        // Calculate offset
        val offset = (page - 1L) * size

        // Get paginated decks
        val decks = Deck
            .join(UserRepo.Users, JoinType.INNER, Deck.owner, UserRepo.Users.id)
            .select(
                Deck.id,
                Deck.hash,
                Deck.name,
                Deck.primarySpec,
                Deck.owner,
                Deck.visibility,
                Deck.format,
                Deck.created,
                Deck.lastModified,
                UserRepo.Users.username
            )
            .where { Deck.visibility eq DeckVisibility.PUBLIC }
            .orderBy(Deck.lastModified, SortOrder.DESC)
            .limit(size)
            .offset(offset)
            .map { row ->
                val deck = Deck.fromResultRow(row)
                deck.ownerName = row[UserRepo.Users.username]
                deck
            }
            .toList()

        return@dbQuery Page(
            items = decks,
            itemCount = decks.size.toLong(),
            totalItems = totalCount,
            page = page,
            pageSize = size,
            totalPages = ((totalCount + size - 1) / size).toInt()
        )
    }

    suspend fun getDeckList(deckHash: String, userId: Uuid? = null): IvionDeck = dbQuery {
        val deck = Deck
            .selectFields()
            .where { Deck.hash eq (deckHash) }
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
            .firstOrNull() ?: return@dbQuery null
            
        return@dbQuery updatedDeck
    }
    
    suspend fun patchSettings(userId: Uuid, deckId: Uuid, settings: DeckPatchSettings) = dbQuery {
        val now = Clock.System.now()

        val updatedDeck = Deck
            .updateReturning(
                where = { Deck.id eq deckId.toJavaUuid() and (Deck.owner eq userId.toJavaUuid()) }
            ) {
                // Only update fields that are provided in the settings object
                settings.name?.let { newName -> it[name] = newName }
                settings.visibility?.let { newVisibility -> it[visibility] = newVisibility }
                settings.format?.let { newFormat -> it[format] = newFormat }
                it[lastModified] = now
            }
            .map { Deck.fromResultRow(it) }
            .firstOrNull() ?: return@dbQuery null
            
        return@dbQuery updatedDeck
    }

    suspend fun deleteDeck(userId: Uuid, deckId: Uuid) = dbQuery {
        return@dbQuery Deck
            .deleteWhere { owner eq userId.toJavaUuid() and (id eq deckId.toJavaUuid()) }
    }

    private fun Deck.fromResultRow(result: ResultRow): IvionDeck =
        IvionDeck(
            result[id].value.toKotlinUuid(),
            result[hash],
            result[name],
            mutableListOf(),
            result[primarySpec],
            result[owner].toKotlinUuid(),
            result[visibility],
            result[format],
            result[created],
            result[lastModified],
            likes = result.getOrNull(DeckFactsAggregate.likes) ?: 0,
            views = result.getOrNull(DeckFactsAggregate.views) ?: 0,
            favorite = !(result.getOrNull(DeckFavorites.deleted) ?: true),
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
            .where { DeckEntry.deckId eq deckId.toJavaUuid() }
            .map { it.toIvionDeckEntry() }
            .toList()

    }

    suspend fun editDeck(userId: Uuid, deckHash: String, cardId: Uuid, count: Int): EditDeckResponse? = dbQuery {
        val deck = Deck
            .selectFields()
            .where { Deck.owner eq userId.toJavaUuid() and (Deck.hash eq deckHash) }
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
            DeckEntry.deleteWhere { deckId eq deckUUID and (DeckEntry.cardId eq cardId.toJavaUuid()) }
        }

//        println((card.toString()))
//        println

        var changeSpec: Boolean = false
        var spec: String? = null

        Deck.update(
            where = { Deck.id eq deckUUID }
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

    private fun ResultRow.toIvionDeckEntry(): IvionDeckEntry =
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

    suspend fun upsertLike(deckId: Uuid, userId: Uuid, deleted: Boolean) = dbQuery {

        val now = Clock.System.now()
        DeckLikes.upsert(
            keys = arrayOf(DeckLikes.deckId, DeckLikes.userId),
            where = {
                (DeckLikes.deckId eq deckId.toJavaUuid()) and (DeckLikes.userId eq userId.toJavaUuid())
            },
            onUpdateExclude = listOf(DeckLikes.id, DeckLikes.deckId, DeckLikes.userId, DeckLikes.timestamp),
        ) {
            it[this.id] = randomUUID()
            it[this.deckId] = deckId.toJavaUuid()
            it[this.userId] = userId.toJavaUuid()
            it[this.deleted] = deleted
            it[timestamp] = now
            if (deleted) {
                it[deletedAt] = now
            }
        }

        modifyLikesAggregate(deckId, deleted)
    }

    suspend fun hasUserLiked(deckId: Uuid, userId: Uuid): Boolean = dbQuery {
        return@dbQuery DeckLikes.selectAll().where {
            DeckLikes.deckId eq deckId.toJavaUuid() and (DeckLikes.userId eq userId.toJavaUuid()) and (DeckLikes.deleted neq true)
        }.limit(1).firstOrNull() != null
    }

    private suspend fun modifyLikesAggregate(deckId: Uuid, deleted: Boolean) = dbQuery {
        val factAggregate = DeckFactsAggregate.selectAll()
            .where { DeckFactsAggregate.deckId eq deckId.toJavaUuid() }
            .map(DeckFactsAggregate::toDeckFacts)
            .getOrElse(0) { DeckFacts(Uuid.random(), deckId, 0, 0) }
        DeckFactsAggregate.upsert(
            keys = arrayOf(DeckFactsAggregate.deckId),
            where = {
                DeckFactsAggregate.deckId eq deckId.toJavaUuid()
            }
        ) {
            it[this.id] = factAggregate.id.toJavaUuid()
            it[this.deckId] = factAggregate.deckId.toJavaUuid()
            it[views] = factAggregate.views
            it[likes] = if (deleted) factAggregate.likes - 1 else factAggregate.likes + 1
        }
    }

    suspend fun upsertFavorite(deckId: Uuid, userId: Uuid, deleted: Boolean) = dbQuery {
        val now = Clock.System.now()
        return@dbQuery DeckFavorites.upsert(
            keys = arrayOf(DeckFavorites.deckId, DeckFavorites.userId),
            where = {
                DeckFavorites.deckId eq deckId.toJavaUuid() and (DeckFavorites.userId eq userId.toJavaUuid())
            },
            onUpdateExclude = listOf(
                DeckFavorites.id,
                DeckFavorites.userId,
                DeckFavorites.deckId,
                DeckFavorites.timestamp
            ),
        ) {
            it[this.id] = randomUUID()
            it[this.deckId] = deckId.toJavaUuid()
            it[this.userId] = userId.toJavaUuid()
            it[this.deleted] = deleted
            it[timestamp] = now
            if (deleted) {
                it[deletedAt] = now
            }
        }.insertedCount > 0

    }

}