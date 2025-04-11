package app.herocraft.features.search

import app.herocraft.core.extensions.DataService
import app.herocraft.core.extensions.ilike
import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.Page
import app.softwork.uuid.toUuidOrNull
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class CardRepo(database: Database) : DataService(database) {
    object Card : Table() {
        val id = uuid("id")
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

        override val primaryKey = PrimaryKey(id)
    }

    private val logger = LoggerFactory.getLogger(CardRepo::class.java)

    suspend fun getOne(id: Uuid) = dbQuery {
        Card
            .selectAll()
            .where{Card.id eq id.toJavaUuid()}
            .limit(1)
            .map { it.toIvionCard() }
            .firstOrNull()
    }

    enum class SearchOps() {
        ARCHETYPE,
        FORMAT,
        FLAVOR,
        NAME,
        TYPE,
        RULES;

        companion object {
            fun parse(string: String): SearchOps =
                when(string.lowercase(Locale.getDefault())) {
                    "t", "type" -> TYPE
                    "a", "archetype", "c", "class" -> ARCHETYPE
                    "f", "format" -> FORMAT
                    "ft", "flavor" -> FLAVOR
                    "r", "rules", "o" -> RULES
                    else -> NAME
                }
        }
    }

    suspend fun search(searchString: String, size: Int = 60, page: Int = 1): Page<IvionCard> {

        val queryTerms = mutableMapOf(SearchOps.NAME to "")


        for (str: String in searchString.split(" ")) {
            if (str.contains(":")) {
                val tokens = str.split(":")
                when (SearchOps.parse(tokens[0])) {
                    SearchOps.TYPE -> queryTerms[SearchOps.TYPE] = tokens[1]
                    SearchOps.ARCHETYPE -> queryTerms[SearchOps.ARCHETYPE] = tokens[1]
                    SearchOps.FORMAT -> queryTerms[SearchOps.FORMAT] = tokens[1]
                    SearchOps.FLAVOR -> queryTerms[SearchOps.FLAVOR] = tokens[1]
                    SearchOps.RULES -> queryTerms[SearchOps.RULES] = tokens[1]
                    else -> {}
                }
            }
            else {
                queryTerms[SearchOps.NAME] += "$str "
            }
        }

        var query = Card.name ilike "%${queryTerms[SearchOps.NAME]?.trim()}%";
        if (queryTerms.contains(SearchOps.TYPE)) {
            query = query and (Card.type ilike "%${queryTerms[SearchOps.TYPE]}%")
        }

        if (queryTerms.contains(SearchOps.ARCHETYPE)) {
            query = query and (Card.archetype ilike "%${queryTerms[SearchOps.ARCHETYPE]}%")
        }

        if (queryTerms.contains(SearchOps.FORMAT)) {
            query = query and (Card.format ilike "%${queryTerms[SearchOps.FORMAT]}%")
        }

        if (queryTerms.contains(SearchOps.FLAVOR)) {
            query = query and (Card.flavorText ilike "%${queryTerms[SearchOps.FLAVOR]}%")
        }

        if (queryTerms.contains(SearchOps.RULES)) {
            query = query and (Card.rulesText ilike "%${queryTerms[SearchOps.RULES]}%")
        }


        return paged(size, page, {Card.id.count()}, {query})
    }

    suspend fun getPaging(size: Int = 60, page: Int = 1) = paged(size, page, {Card.id.count()}, {Op.TRUE})

    private suspend fun paged(
        size: Int  = 60,
        page: Int = 1,
        total: () -> Count = {Card.id.count()},
        query: (SqlExpressionBuilder.() -> Op<Boolean>))
    = dbQuery {
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

        val offset = (page-1L)*size
        val cards = Card
            .selectAll()
            .where(query)
            .orderBy(Card.name, SortOrder.ASC)
            .limit(size)
            .offset(offset)
            .map {
                it.toIvionCard()
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
            this[Card.id].toKotlinUuid(),
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
}
