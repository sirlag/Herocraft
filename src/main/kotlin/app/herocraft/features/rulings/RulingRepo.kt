package app.herocraft.features.rulings

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.Ruling
import app.herocraft.core.models.RulingInput
import app.herocraft.core.models.RulingSource
import app.herocraft.core.models.RulingStyle
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
// no-op imports; rely on in-memory sorting for DAO results
import kotlin.time.Clock
import kotlin.time.Instant
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class RulingRepo(database: Database) : DataService(database) {

    object Rulings : UUIDTable("rulings") {
        val cardHerocraftId = uuid("card_herocraft_id")
        // Named src to avoid shadowing ColumnSet.source
        val src = text("source")
        val sourceUrl = text("source_url").nullable()
        val publishedAt = timestamp("published_at")
        val style = text("style")
        val comment = text("comment").nullable()
        val question = text("question").nullable()
        val answer = text("answer").nullable()
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
    }

    class RulingEntity(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<RulingEntity>(Rulings)

        val cardHerocraftId by Rulings.cardHerocraftId
        val source by Rulings.src
        val sourceUrl by Rulings.sourceUrl
        val publishedAt by Rulings.publishedAt
        val style by Rulings.style
        val comment by Rulings.comment
        val question by Rulings.question
        val answer by Rulings.answer
        val createdAt by Rulings.createdAt
        val updatedAt by Rulings.updatedAt
    }

    private fun validate(input: RulingInput) {
        // Source validation (service-layer enum already constrains values)
        require(input.source in setOf(RulingSource.discord, RulingSource.luminary, RulingSource.herocraft))

        when (input.style) {
            RulingStyle.RULING -> require(!input.comment.isNullOrBlank()) { "comment is required for RULING" }
            RulingStyle.QA -> require(!(input.question.isNullOrBlank() && input.answer.isNullOrBlank())) { "question or answer required for QA" }
        }
        // Basic URL validation if present (very light)
        input.sourceUrl?.let {
            require(it.length <= 2048) { "sourceUrl too long" }
        }
    }

    private fun entityToModel(e: RulingEntity): Ruling = Ruling(
        id = e.id.value.toKotlinUuid(),
        cardHerocraftId = e.cardHerocraftId.toKotlinUuid(),
        source = when (e.source) {
            "discord" -> RulingSource.discord
            "luminary" -> RulingSource.luminary
            else -> RulingSource.herocraft
        },
        sourceUrl = e.sourceUrl,
        publishedAt = e.publishedAt,
        style = when (e.style) {
            "QA" -> RulingStyle.QA
            else -> RulingStyle.RULING
        },
        comment = e.comment,
        question = e.question,
        answer = e.answer,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
    )

    suspend fun listForCard(cardHerocraftId: Uuid): List<Ruling> = dbQuery {
        RulingEntity
            .find { Rulings.cardHerocraftId eq cardHerocraftId.toJavaUuid() }
            .map { entityToModel(it) }
            .sortedByDescending { it.publishedAt }
    }

    suspend fun create(input: RulingInput): Ruling = dbQuery {
        validate(input)
        val now = Clock.System.now()
        val stmt = Rulings.insert {
            it[id] = UUID.randomUUID()
            it[cardHerocraftId] = input.cardHerocraftId.toJavaUuid()
            it[src] = input.source.name
            it[sourceUrl] = input.sourceUrl
            it[publishedAt] = input.publishedAt
            it[style] = input.style.name
            it[comment] = input.comment
            it[question] = input.question
            it[answer] = input.answer
            it[createdAt] = now
            it[updatedAt] = now
        }
        val newId = stmt[Rulings.id].value
        entityToModel(RulingEntity[newId])
    }

    suspend fun update(id: Uuid, input: RulingInput): Ruling = dbQuery {
        validate(input)
        val now = Clock.System.now()
        val count = Rulings.update(where = { Rulings.id eq id.toJavaUuid() }) {
            it[cardHerocraftId] = input.cardHerocraftId.toJavaUuid()
            it[src] = input.source.name
            it[sourceUrl] = input.sourceUrl
            it[publishedAt] = input.publishedAt
            it[style] = input.style.name
            it[comment] = input.comment
            it[question] = input.question
            it[answer] = input.answer
            it[updatedAt] = now
        }
        require(count > 0) { "Ruling not found" }

        entityToModel(RulingEntity[id.toJavaUuid()])
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        val deleted = Rulings.deleteWhere { Rulings.id eq id.toJavaUuid() }
        deleted > 0
    }
}
