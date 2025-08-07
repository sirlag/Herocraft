package app.herocraft.core.extensions

import app.softwork.uuid.isValidUuidString
import app.softwork.uuid.toUuid
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.util.*
import kotlin.uuid.Uuid

fun String?.isEmailAddress(): Boolean = this != null && contains("@") && contains(".")

fun String?.isUuid(): Boolean = this != null && Uuid.isValidUuidString(this)
fun String?.isValidShort(): Boolean = this != null && this.length == 22

fun String.toUuidFromShort(): Uuid =
    Uuid.fromByteArray(
    (this.replace("_", "/")
        .replace("-", "+")
        + "==")
        .decodeBase64Bytes())

fun String?.tryUuid(): Uuid? {
    return when {
        this.isUuid() -> this?.toUuid()
        this.isValidShort() -> this?.toUuidFromShort()
        else -> null
    }
}

//    id.toByteArray().encodeBase64()
//    .replace("/", "_")
//    .replace("+", "-")
//    .substring(0, 22)