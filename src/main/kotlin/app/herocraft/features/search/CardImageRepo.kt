package app.herocraft.features.search

import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.IvionCardImage
import app.herocraft.features.search.CardRepo.Card
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import kotlin.time.toJavaInstant
import kotlin.uuid.toJavaUuid

class CardImageRepo(database: Database): DataService(database) {

    object CardImage: Table("cardimages") {
        val id = uuid("image_id")
        val card_id = reference(
            name = "card_id",
            refColumn = Card.id,
            onDelete = ReferenceOption.CASCADE)
        val variant = text("variant")
        val face = varchar("face", 5)
        val uri = text("uri")
        val mimeType = varchar(name = "mime_type", length = 100)
        val byteSize = integer("byte_size")
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")
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