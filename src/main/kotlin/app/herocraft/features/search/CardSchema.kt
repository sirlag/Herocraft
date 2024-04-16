package app.herocraft.features.search

import app.herocraft.core.models.IvionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.io.File

class CardService(private val database: Database) {
    object Card : Table() {
        val id = uuid("id")
        val collectorsNumber = integer("collectors_number").nullable()
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
        val colorPip1 = text("color_pip_1").nullable()
        val colorPip2 = text("color_pip_2").nullable()
        val season = text("season")
        val type = text("type").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    val logger = LoggerFactory.getLogger(CardService::class.java)

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getPaging(size: Int  = 60, page: Int = 1) = dbQuery {
        val totalCount = Card.id.count().over().alias("total_count")
        val offset = (page-1L)*size
        return@dbQuery Card
            .selectAll()
            .orderBy(Card.name, SortOrder.ASC)
            .limit(size, offset)
            .map {
                Card.fromResultRow(it)
            }.toList()
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
                        collectorsNumber = cols[0].toIntOrNull(),
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
                        ivionUUID = UUID(cols[18]),
                        colorPip1 = cols[19],
                        colorPip2 = cols[20],
                        season = cols[21],
                        type = cols[22]
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
                }
            }
        } else {
            logger.error("Unable to load cards")
        }

        return cards;
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
        it[flavorText] = card.flavorText
        it[artist] = card.artist
        it[ivionUUID] = card.ivionUUID.toJavaUUID()
        it[colorPip1] = card.colorPip1
        it[colorPip2] = card.colorPip2
        it[season] = card.season
        it[type] = card.type
    }

    private fun Card.fromResultRow(result: ResultRow): IvionCard =
        IvionCard(
            result[id].toKotlinUUID(),
            result[collectorsNumber],
            result[format],
            result[name],
            result[archetype],
            result[actionCost],
            result[powerCost],
            result[range],
            result[health],
            result[heroic],
            result[slow],
            result[silence],
            result[disarm],
            result[extraType],
            result[rulesText],
            result[flavorText],
            result[artist],
            result[ivionUUID].toKotlinUUID(),
            result[colorPip1],
            result[colorPip2],
            result[season],
            result[type]
        )
}
