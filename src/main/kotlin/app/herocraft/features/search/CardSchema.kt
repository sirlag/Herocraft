package app.herocraft.features.search

import app.herocraft.antlr.generated.HQLLexer
import app.herocraft.antlr.generated.HQLParser
import app.herocraft.core.extensions.DataService
import app.herocraft.core.extensions.ilike
import app.herocraft.core.models.CardFace
import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.IvionCardFaceData
import app.herocraft.core.models.IvionCardImageURIs
import app.herocraft.core.models.Page
import app.softwork.uuid.toUuidOrNull
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.select
import org.slf4j.LoggerFactory
import java.util.*
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
        // Optional linkage id used for rulings/legality aggregation
        val herocraftId = uuid("herocraft_id").nullable()

    }

    /**
     * New table to store per-face textual information for cards.
     * This relation replaces any former per-face overrides on the card table.
     */
    object CardFaces : UUIDTable(name = "card_faces", columnName = "id") {
        val card = reference("card_id", Card)
        // store face as varchar, accepts either 'front'/'back' or 'FRONT'/'BACK'
        val face = varchar("face", 8)
        val name = text("name").nullable()
        val rulesText = text("rules_text").nullable()
        val flavorText = text("flavor_text").nullable()
        val artist = text("artist").nullable()
        // Additional per-face fields for when a face represents a distinct card
        val actionCost = integer("action_cost").nullable()
        val powerCost = integer("power_cost").nullable()
        val heroic = bool("heroic").nullable()
        val slow = bool("slow").nullable()
        val silence = bool("silence").nullable()
        val disarm = bool("disarm").nullable()

        init {
            // unique per card per face
            uniqueIndex("unq_card_faces_card_face", card, face)
        }
    }

    class CardFaceEntity(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<CardFaceEntity>(CardFaces)

        val card by CardEntity referencedOn CardFaces.card
        val face by CardFaces.face
        val name by CardFaces.name
        val rulesText by CardFaces.rulesText
        val flavorText by CardFaces.flavorText
        val artist by CardFaces.artist
        val actionCost by CardFaces.actionCost
        val powerCost by CardFaces.powerCost
        val heroic by CardFaces.heroic
        val slow by CardFaces.slow
        val silence by CardFaces.silence
        val disarm by CardFaces.disarm

        fun toFaceData(images: SizedIterable<CardImageRepo.CardImageEntity>): IvionCardFaceData {
            val faceEnum = when (face.lowercase()) {
                "front" -> CardFace.FRONT
                "back" -> CardFace.BACK
                "FRONT" -> CardFace.FRONT
                "BACK" -> CardFace.BACK
                else -> try {
                    CardFace.valueOf(face.uppercase())
                } catch (e: Exception) {
                    CardFace.FRONT
                }
            }
            // Build image URIs for this face from images iterable
            val faceKey = faceEnum.toString()
            var full = ""
            var large = ""
            var normal = ""
            var small = ""
            images.filter { it.face == faceKey }.forEach {
                if (it.variant == "full") full = it.uri
                if (it.variant == "large") large = it.uri
                if (it.variant == "normal") normal = it.uri
                if (it.variant == "small") small = it.uri
            }
            val uris = if (full.isNotEmpty() || large.isNotEmpty() || normal.isNotEmpty() || small.isNotEmpty())
                IvionCardImageURIs(full, large, normal, small) else null
            return IvionCardFaceData(
                face = faceEnum,
                name = name,
                rulesText = rulesText,
                flavorText = flavorText,
                artist = artist,
                imageUris = uris,
                actionCost = actionCost,
                powerCost = powerCost,
                heroic = heroic,
                slow = slow,
                silence = silence,
                disarm = disarm
            )
        }
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
        val herocraftIdOpt by Card.herocraftId
        val colorPip1 by Card.colorPip1
        val colorPip2 by Card.colorPip2
        val season by Card.season
        val type by Card.type

        val images by CardImageRepo.CardImageEntity referrersOn CardImageRepo.CardImage.card_id
        val facesInfo by CardFaceEntity referrersOn CardFaces.card


        fun SizedIterable<CardImageRepo.CardImageEntity>.getImageUris(): IvionCardImageURIs? {
            if (this.empty()) return null

            var full: String = ""
            var large: String = ""
            var normal: String = ""
            var small: String = ""

            this.filter { it.face == "front" }.forEach {
                if (it.variant == "full") full = it.uri
                if (it.variant == "large") large = it.uri
                if (it.variant == "normal") normal = it.uri
                if (it.variant == "small") small = it.uri
            }

            return if (full.isNotEmpty() || large.isNotEmpty() || normal.isNotEmpty() || small.isNotEmpty())
                IvionCardImageURIs(full, large, normal, small) else null
        }

        fun SizedIterable<CardImageRepo.CardImageEntity>.getFaceImageUris(face: CardFace): IvionCardImageURIs? {
            if (this.empty()) return null
            val faceKey = face.toString()

            var full: String = ""
            var large: String = ""
            var normal: String = ""
            var small: String = ""

            this.filter { it.face == faceKey }.forEach {
                if (it.variant == "full") full = it.uri
                if (it.variant == "large") large = it.uri
                if (it.variant == "normal") normal = it.uri
                if (it.variant == "small") small = it.uri
            }

            return if (full.isNotEmpty() || large.isNotEmpty() || normal.isNotEmpty() || small.isNotEmpty())
                IvionCardImageURIs(full, large, normal, small) else null
        }

        fun toIvionCard(): IvionCard {

            val frontUris = images.getFaceImageUris(CardFace.FRONT)
            val backUris = images.getFaceImageUris(CardFace.BACK)

            // Build faces from new relation if present; fall back to legacy images only
            val facesFromTable = facesInfo.map { it.toFaceData(images) }.toList()

            val faceList = buildList<IvionCardFaceData> {
                if (facesFromTable.isNotEmpty()) {
                    addAll(facesFromTable)
                } else {
                    // Legacy behavior: FRONT from base fields when we have images; BACK only when images exist
                    if (frontUris != null) {
                        add(
                            IvionCardFaceData(
                                face = CardFace.FRONT,
                                name = name,
                                rulesText = rulesText,
                                flavorText = flavorText,
                                artist = artist,
                                imageUris = frontUris
                            )
                        )
                    }
                    if (backUris != null) {
                        add(
                            IvionCardFaceData(
                                face = CardFace.BACK,
                                name = name,
                                rulesText = rulesText,
                                flavorText = flavorText,
                                artist = artist,
                                imageUris = backUris
                            )
                        )
                    }
                }
            }.ifEmpty { null }

            val inferredLayout = when {
                backUris != null || secondUUID != null -> app.herocraft.core.models.CardLayout.TRANSFORM
                else -> app.herocraft.core.models.CardLayout.NORMAL
            }

            val hcId = herocraftIdOpt?.toKotlinUuid()

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
                layout = inferredLayout,
                imageUris = images.getImageUris(),
                faces = faceList,
                // New fields defaulted until DB layer supports them
                linkedParts = emptyList(),
                herocraftId = hcId,
                printVariantGroupId = null,
                variants = emptyList(),
                rulingsUri = hcId?.let { "/cards/${it}/rulings" }
            )
        }

    }


    private val logger = LoggerFactory.getLogger(CardRepo::class.java)

    // Basic CRUD operations for admin
    suspend fun create(card: IvionCard): IvionCard = dbQuery {
        val statement = Card.insert {
            fromIvionCard(it, card)
        }
        val newId = statement[Card.id].value
        // Persist faces to relation table
        saveFaces(newId, card.faces)
        CardEntity[newId].toIvionCard()
    }

    suspend fun update(id: Uuid, card: IvionCard): IvionCard = dbQuery {
        // Update fields using Exposed DSL similar to UserRepo
        Card.update(
            where = { Card.id eq id.toJavaUuid() }
        ) {
            it[Card.collectorsNumber] = card.collectorsNumber
            it[Card.format] = card.format
            it[Card.name] = card.name
            it[Card.archetype] = card.archetype
            it[Card.actionCost] = card.actionCost
            it[Card.powerCost] = card.powerCost
            it[Card.range] = card.range
            it[Card.health] = card.health
            it[Card.heroic] = card.heroic
            it[Card.slow] = card.slow
            it[Card.silence] = card.silence
            it[Card.disarm] = card.disarm
            it[Card.extraType] = card.extraType
            it[Card.rulesText] = card.rulesText
            it[Card.flavorText] = card.flavorText
            it[Card.artist] = card.artist
            it[Card.ivionUUID] = card.ivionUUID.toJavaUuid()
            it[Card.secondUUID] = card.secondUUID?.toJavaUuid()
            // herocraft_id defaults to ivion_uuid if not explicitly provided
            it[Card.herocraftId] = (card.herocraftId ?: card.ivionUUID).toJavaUuid()
            it[Card.colorPip1] = card.colorPip1
            it[Card.colorPip2] = card.colorPip2
            it[Card.season] = card.season
            it[Card.type] = card.type
        }
        // Replace faces with those from payload
        saveFaces(id.toJavaUuid(), card.faces)
        CardEntity[id.toJavaUuid()].toIvionCard()
    }

    suspend fun delete(id: Uuid): Boolean = dbQuery {
        val deleted = Card.deleteWhere { Card.id eq id.toJavaUuid() }
        deleted > 0
    }

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
            SearchField.COLOR -> Card.colorPip1 to String::class
            SearchField.FORMAT -> Card.format to String::class
            SearchField.FLAVOR -> Card.flavorText to String::class
            SearchField.NAME -> Card.name to String::class
            SearchField.RULES -> Card.rulesText to String::class
            SearchField.SEASON -> Card.season to String::class
            SearchField.TYPE -> Card.type to String::class
            SearchField.UNKNOWN -> null
        }


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
            return paged(size, page, { Card.id.count() }, { Op.TRUE })
        }


        return paged(size, page, { Card.id.count() }, { query })
    }

    suspend fun getPaging(size: Int = 60, page: Int = 1) = paged(size, page, { Card.id.count() }, { Op.TRUE })

    private suspend fun paged(
        size: Int = 60,
        page: Int = 1,
        total: () -> Count = { Card.id.count() },
        query: (() -> Op<Boolean>)
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
            .map(CardEntity::toIvionCard)
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
        // default linkage is ivion_uuid unless specified
        it[herocraftId] = (card.herocraftId ?: card.ivionUUID).toJavaUuid()
        it[colorPip1] = card.colorPip1
        it[colorPip2] = card.colorPip2
        it[season] = card.season
        it[type] = card.type
    }

    private fun normalizeFaceString(face: CardFace): String = face.name // store as 'FRONT'/'BACK'

    private fun saveFaces(cardId: UUID, faces: List<IvionCardFaceData>?) {
        if (faces == null) return
        // Delete existing faces for this card and insert fresh
        CardFaces.deleteWhere { CardFaces.card eq EntityID(cardId, Card) }
        faces.forEach { f ->
            CardFaces.insert { st ->
                st[CardFaces.card] = EntityID(cardId, Card)
                st[face] = normalizeFaceString(f.face)
                st[name] = f.name
                st[rulesText] = f.rulesText
                st[flavorText] = f.flavorText
                st[artist] = f.artist
                st[actionCost] = f.actionCost
                st[powerCost] = f.powerCost
                st[heroic] = f.heroic
                st[slow] = f.slow
                st[silence] = f.silence
                st[disarm] = f.disarm
            }
        }
    }

    private fun ResultRow.toIvionCard(): IvionCard {
        val hcId = this[Card.herocraftId]?.toKotlinUuid()
        return IvionCard(
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
            type = this[Card.type],
            herocraftId = hcId,
            rulingsUri = hcId?.let { "/cards/${it}/rulings" }
        )
    }
}
