package app.herocraft.features.search

import app.herocraft.antlr.generated.HQLLexer
import app.herocraft.antlr.generated.HQLParser
import app.herocraft.core.extensions.DataService
import app.herocraft.core.extensions.ilike
import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.IvionCardImageURIs
import app.herocraft.core.models.Page
import app.herocraft.features.search.CardRepo.CardEntity.Companion.referrersOn
import app.softwork.uuid.toUuidOrNull
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

typealias Query = Pair<Op<Boolean>?, List<SearchItem>?>

class CardRepo(database: Database) : DataService(database) {
    object Card : UUIDTable(name = "card", columnName = "id") {
        val collectorsNumber = text("collectors_number").nullable()
        val format = text("format").nullable()
        val name = text("name")
        val archetype = text("archetype").nullable()
        val actionCost = integer("action_cost").nullable()
        val powerCost = integer("power_cost").nullable()
        val range = integer("range").nullable()
        val health = integer("health").nullable()
        val heroic = bool("heroic")
        val slow = bool("slow")
        val silence = bool("silence")
        val disarm = bool("disarm")
        val extraType = text("extra_type").nullable()
        val rulesText = text("rules_text").nullable()
        val flavorText = text("flavor_text").nullable()
        val artist = text("artist")
        val ivionUUID = uuid("ivion_uuid")
        val secondUUID = uuid("second_uuid").nullable()
        val colorPip1 = text("color_pip_1").nullable()
        val colorPip2 = text("color_pip_2").nullable()
        val season = text("season")
        val type = text("type").nullable()

    }

    class CardEntity(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<CardEntity>(Card)
        val collectorsNumber by Card.collectorsNumber
        val format by Card.format
        val name by Card.name
        val archetype by Card.archetype
        val actionCost by Card.actionCost
        val powerCost by Card.powerCost
        val range by Card.range
        val health by Card.health
        val heroic by Card.heroic
        val slow by Card.slow
        val silence by Card.silence
        val disarm by Card.disarm
        val extraType by Card.extraType
        val rulesText by Card.rulesText
        val flavorText by Card.flavorText
        val artist by Card.artist
        val ivionUUID by Card.ivionUUID
        val secondUUID by Card.secondUUID
        val colorPip1 by Card.colorPip1
        val colorPip2 by Card.colorPip2
        val season by Card.season
        val type by Card.type

        val images by CardImageRepo.CardImageEntity referrersOn  CardImageRepo.CardImage.card_id


        fun SizedIterable<CardImageRepo.CardImageEntity>.getImageUris(): IvionCardImageURIs? {
            // TODO: Add support for getting backs
            if (this.empty()) return null

            var full: String = ""
            var large: String = ""
            var normal: String = ""
            var small: String = ""

            this.filter { it.face == "front" }.forEach {
                if (it.variant == "full")
                    full = it.uri
                if (it.variant == "large")
                    large = it.uri
                if (it.variant == "normal")
                    normal = it.uri
                if (it.variant == "small")
                    small = it.uri
            }

            return IvionCardImageURIs(full, large, normal, small)
        }

        fun toIvionCard(): IvionCard {

            return IvionCard(
                id = id.value.toKotlinUuid(),
                collectorsNumber = collectorsNumber,
                format = format,
                name = name,
                archetype = archetype,
                actionCost = actionCost,
                powerCost = powerCost,
                range = range,
                health = health,
                heroic = heroic,
                slow = slow,
                silence = silence,
                disarm = disarm,
                extraType = extraType,
                rulesText = rulesText,
                flavorText = flavorText,
                artist = artist,
                ivionUUID = ivionUUID.toKotlinUuid(),
                secondUUID = secondUUID?.toKotlinUuid(),
                colorPip1 = colorPip1,
                colorPip2 = colorPip2,
                season = season,
                type = type,
                imageUris = images.getImageUris()
            )
        }

    }


    private val logger = LoggerFactory.getLogger(CardRepo::class.java)

    suspend fun getOne(id: Uuid) = dbQuery {

        return@dbQuery CardEntity[id.toJavaUuid()].toIvionCard()

    }

    suspend fun getAll() = dbQuery {
        CardEntity.all().map { it.toIvionCard() }.toList()
    }


    private fun colummMap(searchField: SearchField) =
        when (searchField) {
            SearchField.ARCHETYPE -> Card.archetype to String::class
            SearchField.ARTIST -> Card.artist to String::class
            SearchField.FORMAT -> Card.format to String::class
            SearchField.FLAVOR -> Card.flavorText to String::class
            SearchField.NAME -> Card.name to String::class
            SearchField.RULES -> Card.rulesText to String::class
            SearchField.TYPE -> Card.type to String::class
            SearchField.UNKNOWN -> null
        }

//    data class Query(val expression: Expression<Boolean>?, val discardedFields: List<SearchField>?)


    fun buildQuery(searchItem: SearchItem): Query {

        return when (searchItem) {
            is BooleanSearchItem -> {

                val (fields, errors) = searchItem.children.map { buildQuery(it) }.toList().unzip()

                when (searchItem.type) {
                    BooleanSearchType.AND -> {
                        Query(AndOp(fields.filterNotNull()), errors.filterNotNull().flatten())
                    }

                    BooleanSearchType.OR -> {
                        Query(OrOp(fields.filterNotNull()), errors.filterNotNull().flatten())

                    }
                }
            }

            is FieldSearchItem -> {

                val column = colummMap(searchItem.field)

                when {
                    column == null -> Query(null, listOf(searchItem))
                    (column.second == String::class) -> Query(column.first ilike "%${searchItem.value}%", emptyList())
                    else -> Query(null, listOf(searchItem))
                }
            }

            is NotSearchItem -> {
                val query = buildQuery(searchItem.child)
                when {
                    (query.first == null) -> Query(null, query.second)
                    else -> Query(NotOp(query.first!!), query.second)
                }
            }

            is ValuesSearchItem, EmptySearchItem, TerminalSearchItem -> throw RuntimeException("IllegalSearchState")
        }
    }

    suspend fun search(searchString: String, size: Int = 60, page: Int = 1): Page<IvionCard> {

        val inputStream = CharStreams.fromString(searchString)
        val lexer = HQLLexer(inputStream)
        val tokens = CommonTokenStream(lexer)
        val parser = HQLParser(tokens)

        val ctx = parser.query()
        val visitor = SearchVisitor()
        val searchField = visitor.visit(ctx)

        val (query, errors) = buildQuery(searchField)

        if (query == null) {
            throw RuntimeException("Empty search string")
        }

        return paged(size, page, { Card.id.count() }, { query })
    }

    suspend fun getPaging(size: Int = 60, page: Int = 1) = paged(size, page, { Card.id.count() }, { Op.TRUE })

    private suspend fun paged(
        size: Int = 60,
        page: Int = 1,
        total: () -> Count = { Card.id.count() },
        query: (SqlExpressionBuilder.() -> Op<Boolean>)
    ) = dbQuery {

        val totalCount = total().alias("total_count")
        val count = Card
            .select(totalCount)
            .where(query)
            .map { it[totalCount] }
            .first()

        if (count == 0L) {
            Page(
                items = emptyList<Card>(),
                itemCount = count,
                totalItems = count,
                page = page,
                pageSize = size,
                totalPages = page,
                hasNext = false
            )
        }

        val offset = (page - 1L) * size

        val cards = CardEntity.find(query)
            .orderBy(Card.name to SortOrder.ASC)
            .limit(size)
            .offset(offset)
            .map (CardEntity::toIvionCard)
            .toList()

//        val cards = Card
//            .selectAll()
//            .where(query)
//            .orderBy(Card.name, SortOrder.ASC)
//            .limit(size)
//            .offset(offset)
//            .map {
//                it.toIvionCard()
//            }.toList()
        Page(
            items = cards,
            itemCount = cards.size.toLong(),
            totalItems = count,
            page = page,
            pageSize = size,
            totalPages = ((count / size) + 1).toInt()
        )
    }


    suspend fun resetTable(): List<IvionCard> {
        val cards = this::class.java.getResourceAsStream("/IvionCardsCombined.tsv")
            ?.bufferedReader()
            ?.use {
                it.lineSequence()
                    .drop(1)
                    .map {
                        val cols = it.split("\t")
                        IvionCard(
                            collectorsNumber = cols[0],
                            format = cols[1],
                            name = cols[2],
                            archetype = cols[3],
                            actionCost = cols[5].toIntOrNull()?.times(if (cols[4] == "-") -1 else 1),
                            powerCost = cols[6].toIntOrNull()?.times(if (cols[7] == "-") -1 else 1),
                            range = cols[8].toIntOrNull(),
                            health = cols[9].toIntOrNull(),
                            heroic = cols[10] == "H",
                            slow = cols[11] == "Slow",
                            silence = cols[12] == "Silence",
                            disarm = cols[13] == "Disarm",
                            extraType = cols[14],
                            rulesText = cols[15],
                            flavorText = cols[16],
                            artist = cols[17],
                            ivionUUID = Uuid.parse(cols[18]),
                            colorPip1 = cols[19],
                            colorPip2 = cols[20],
                            season = cols[21],
                            type = cols[22],
                            secondUUID = cols[23].toUuidOrNull()
                        )
                    }
                    .toList()
            } ?: emptyList()
        if (cards.isNotEmpty()) {
            dbQuery {
                Card.deleteAll()
                cards.forEach { card ->
                    Card.insert {
                        Card.fromIvionCard(it, card)
                    }

                    Card.insert {
                        it[collectorsNumber]
                    }
                }
            }
        } else {
            logger.error("Unable to load cards")
        }

        return cards
    }

    private fun Card.fromIvionCard(it: InsertStatement<Number>, card: IvionCard) {
        it[collectorsNumber] = card.collectorsNumber
        it[format] = card.format
        it[name] = card.name
        it[archetype] = card.archetype
        it[actionCost] = card.actionCost
        it[powerCost] = card.powerCost
        it[range] = card.range
        it[health] = card.health
        it[heroic] = card.heroic
        it[slow] = card.slow
        it[silence] = card.silence
        it[disarm] = card.disarm
        it[extraType] = card.extraType
        it[rulesText] = card.rulesText
        it[flavorText] = card.flavorText
        it[artist] = card.artist
        it[ivionUUID] = card.ivionUUID.toJavaUuid()
        it[secondUUID] = card.secondUUID?.toJavaUuid()
        it[colorPip1] = card.colorPip1
        it[colorPip2] = card.colorPip2
        it[season] = card.season
        it[type] = card.type
    }

    private fun ResultRow.toIvionCard(): IvionCard =
        IvionCard(
            id = this[Card.id].value.toKotlinUuid(),
            collectorsNumber = this[Card.collectorsNumber],
            format = this[Card.format],
            name = this[Card.name],
            archetype = this[Card.archetype],
            actionCost = this[Card.actionCost],
            powerCost = this[Card.powerCost],
            range = this[Card.range],
            health = this[Card.health],
            heroic = this[Card.heroic],
            slow = this[Card.slow],
            silence = this[Card.silence],
            disarm = this[Card.disarm],
            extraType = this[Card.extraType],
            rulesText = this[Card.rulesText],
            flavorText = this[Card.flavorText],
            artist = this[Card.artist],
            ivionUUID = this[Card.ivionUUID].toKotlinUuid(),
            secondUUID = this[Card.secondUUID]?.toKotlinUuid(),
            colorPip1 = this[Card.colorPip1],
            colorPip2 = this[Card.colorPip2],
            season = this[Card.season],
            type = this[Card.type]
        )
}
