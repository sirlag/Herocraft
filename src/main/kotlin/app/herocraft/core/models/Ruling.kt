package app.herocraft.core.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class RulingStyle { QA, RULING }

@Serializable
enum class RulingSource { discord, luminary, herocraft }

@Serializable
data class Ruling(
    val id: Uuid,
    val cardHerocraftId: Uuid,
    val source: RulingSource,
    val sourceUrl: String? = null,
    val publishedAt: Instant,
    val style: RulingStyle,
    val comment: String? = null,
    val question: String? = null,
    val answer: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

// Input DTO for create/update operations
@Serializable
data class RulingInput(
    val cardHerocraftId: Uuid,
    val source: RulingSource,
    val sourceUrl: String? = null,
    val publishedAt: Instant,
    val style: RulingStyle,
    val comment: String? = null,
    val question: String? = null,
    val answer: String? = null,
)
