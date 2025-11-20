package app.herocraft.features.rulings

import app.herocraft.core.models.ErrorMessage
import app.herocraft.core.models.Ruling
import app.herocraft.core.models.RulingInput
import app.herocraft.core.security.UserRepo
import app.herocraft.features.search.CardRepo
import app.herocraft.plugins.UserSession
import app.softwork.uuid.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.uuid.Uuid

fun Application.registerRulingsRoutes(
    rulingRepo: RulingRepo,
    cardRepo: CardRepo,
    userRepo: UserRepo,
) {
    routing {

        // Public endpoint: list rulings for a card by internal id in URL.
        get("/cards/{id}/rulings") {
            val idParam = call.parameters["id"]
            if (idParam.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_REQUEST", "Missing card id"))
                return@get
            }

            val cardId = try { idParam.toUuid() } catch (ex: Throwable) {
                call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_ID", "Invalid card id format"))
                return@get
            }

            // For now, rulings are keyed by the internal id (soft link). If/when herocraftId is added to cards,
            // we can resolve it here and pass that instead.
            val rulings: List<Ruling> = rulingRepo.listForCard(cardId)
            call.respond(HttpStatusCode.OK, rulings)
        }

        // Admin endpoints: create/update/delete rulings
        authenticate("auth-session") {
            route("/admin/rulings") {
                get {
                    val principal = call.authentication.principal<UserSession>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@get
                    }
                    val isAdmin = userRepo.isAdmin(Uuid.parse(principal.id))
                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@get
                    }

                    val cardParam = call.request.queryParameters["cardHerocraftId"]
                    val cardHerocraftId = try { cardParam?.let { Uuid.parse(it) } } catch (ex: Throwable) { null }
                    if (cardHerocraftId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_REQUEST", "cardHerocraftId is required"))
                        return@get
                    }

                    val rulings = rulingRepo.listForCard(cardHerocraftId)
                    call.respond(HttpStatusCode.OK, rulings)
                }
                post {
                    val principal = call.authentication.principal<UserSession>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@post
                    }
                    val isAdmin = userRepo.isAdmin(Uuid.parse(principal.id))
                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@post
                    }

                    val input = call.receive<RulingInput>()
                    val created = rulingRepo.create(input)
                    call.respond(HttpStatusCode.Created, created)
                }

                put("/{id}") {
                    val principal = call.authentication.principal<UserSession>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@put
                    }
                    val isAdmin = userRepo.isAdmin(Uuid.parse(principal.id))
                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@put
                    }

                    val idParam = call.parameters["id"]
                    val rulingId = try { idParam?.let { Uuid.parse(it) } } catch (ex: Throwable) { null }
                    if (rulingId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_ID", "Invalid ruling id"))
                        return@put
                    }

                    val input = call.receive<RulingInput>()
                    val updated = rulingRepo.update(rulingId, input)
                    call.respond(HttpStatusCode.OK, updated)
                }

                delete("/{id}") {
                    val principal = call.authentication.principal<UserSession>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@delete
                    }
                    val isAdmin = userRepo.isAdmin(Uuid.parse(principal.id))
                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@delete
                    }

                    val idParam = call.parameters["id"]
                    val rulingId = try { idParam?.let { Uuid.parse(it) } } catch (ex: Throwable) { null }
                    if (rulingId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_ID", "Invalid ruling id"))
                        return@delete
                    }

                    val deleted = rulingRepo.delete(rulingId)
                    if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
