package app.herocraft.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val items: List<T>,
    val itemCount: Long = 0,
    val totalItems: Long = 0,
    val page: Int = 0,
    val totalPages: Int = 0,
    val pageSize: Int = 0,
    val hasNext: Boolean = page < totalPages,
)