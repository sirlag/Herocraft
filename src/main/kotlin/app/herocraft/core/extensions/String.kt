package app.herocraft.core.extensions

import app.softwork.uuid.toUuid
import io.ktor.util.*
import kotlin.uuid.Uuid

fun String?.isEmailAddress(): Boolean = this != null && contains("@") && contains(".")

fun String.toUuidFromShort(): Uuid =
    Uuid.fromByteArray(
    (this.replace("_", "/")
        .replace("-", "+")
        + "==")
        .decodeBase64Bytes())



//    id.toByteArray().encodeBase64()
//    .replace("/", "_")
//    .replace("+", "-")
//    .substring(0, 22)