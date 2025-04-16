package app.herocraft.core.extensions

import app.herocraft.core.models.ErrorMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*


//suspend inline fun <reified T : Any> ApplicationCall.respondError(throwable: Throwable) {
//
//    val error = ErrorMessage(throwable)
//
//    response.status(error.status)
//    respond(error)
//}
//
//suspend inline fun <reified T : Any> ApplicationCall.respondError(error: ErrorMessage) {
//
//    response.status(error.status)
//    respond(error)
//}