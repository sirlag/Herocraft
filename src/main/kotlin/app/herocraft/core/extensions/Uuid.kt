package app.herocraft.core.extensions

import io.ktor.util.*
import kotlin.uuid.Uuid

fun Uuid.toShortString() = this.toByteArray().encodeBase64()
    .replace("/", "_")
    .replace("+", "-")
    .substring(0, 22)
