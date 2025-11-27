package app.herocraft.features.search

import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.Page
import io.github.oshai.kotlinlogging.KotlinLogging

class SearchService(private val cardRepo: CardRepo) {

    private val logger = KotlinLogging.logger {}

    suspend fun search(
        queryString: String?,
        classes: List<String>?,
        specs: List<String>?,
        types: List<String>?,
        page: Int = 1,
        sortBy: String? = null,
    ): Page<IvionCard> {
        return if (
            queryString.isNullOrEmpty()
            && classes.isNullOrEmpty()
            && specs.isNullOrEmpty()
            && types.isNullOrEmpty()
        ) {
            cardRepo.getPaging(page = page, sortBy = sortBy)
        } else {
            val query = buildQuery(queryString, classes, specs, types)
            logger.debug { "Query: $query" }
            cardRepo.search(query, page = page, sortBy = sortBy)
        }
    }

    private fun buildQuery(
        queryString: String?,
        classes: List<String>?,
        specs: List<String>?,
        types: List<String>?,
    ): String {

        logger.debug { "Query string: $queryString, classes: $classes, specs: $specs, types: $types" }

        val archetypeQuery = (classes.orEmpty() + specs.orEmpty())
            .filter { it.isNotEmpty() }
            .flatMap { it.split(",") }
            .joinToString(" OR ") { "a:$it" }

        logger.debug { "Archetype query: $archetypeQuery" }

        val formatQuery = (types.orEmpty())
            .filter { it.isNotEmpty() }
            .flatMap { it.split(",") }
            .joinToString(" OR ") { "f:$it" }

        val filterQuery = when {
            archetypeQuery.isNotEmpty() && formatQuery.isNotEmpty() -> "($archetypeQuery AND $formatQuery)"
            archetypeQuery.isNotEmpty() -> "($archetypeQuery)"
            formatQuery.isNotEmpty() -> "($formatQuery)"
            else -> ""
        }

        return when {
            filterQuery.isNotEmpty() && !queryString.isNullOrEmpty() -> "$filterQuery $queryString"
            filterQuery.isNotEmpty() -> filterQuery
            else -> queryString.orEmpty()
        }

    }
}