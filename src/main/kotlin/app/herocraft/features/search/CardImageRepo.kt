package app.herocraft.features.search

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.IvionCardImage
import app.herocraft.features.search.CardRepo.Card
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*
import kotlin.uuid.toJavaUuid

class CardImageRepo(database: Database) : DataService(database) {

    object CardImage : UUIDTable("cardimages", "image_id") {
        val card_id = reference(
            name = "card_id",
            refColumn = Card.id,
            onDelete = ReferenceOption.CASCADE
        )
        val variant = text("variant")
        val face = varchar("face", 5)
        val uri = text("uri")
        val mimeType = varchar(name = "mime_type", length = 100)
        val byteSize = integer("byte_size")
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

    }

    class CardImageEntity(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<CardImageEntity>(CardImage)

        val card by CardRepo.CardEntity referencedOn CardImage.card_id
        val variant by CardImage.variant
        val face by CardImage.face
        val uri by CardImage.uri
        val mimeType by CardImage.mimeType
        val byteSize by CardImage.byteSize
        val createdAt by CardImage.createdAt
        val updatedAt by CardImage.updatedAt
    }

    suspend fun addAllCardImages(cards: List<IvionCardImage>) = dbQuery {
        CardImage.batchInsert(cards) {
            this[CardImage.id] = it.id.toJavaUuid()
            this[CardImage.card_id] = it.cardId.toJavaUuid()
            this[CardImage.variant] = it.variant.toString()
            this[CardImage.face] = it.face.toString()
            this[CardImage.uri] = it.uri
            this[CardImage.mimeType] = it.mimeType
            this[CardImage.byteSize] = it.byteSize
            this[CardImage.createdAt] = it.createdAt
            this[CardImage.updatedAt] = it.updatedAt
        }
    }

}