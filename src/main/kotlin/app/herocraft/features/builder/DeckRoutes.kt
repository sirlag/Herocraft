package app.herocraft.features.builder

import app.herocraft.core.extensions.isUuid
import app.herocraft.core.extensions.isValidShort
import app.herocraft.core.extensions.toUuidFromShort
import app.herocraft.core.extensions.tryUuid
import app.herocraft.plugins.UserSession
import app.softwork.uuid.isValidUuidString
import app.softwork.uuid.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlin.uuid.Uuid

fun Application.registerBuilder(deckRepo: DeckRepo) {
    routing {

        get("/deck/{id}") {

            val session = call.sessions.get<UserSession>()

            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val deckList = deckRepo.getDeckList(id, session?.id?.toUuid())

            session?.let { deckList.liked = deckRepo.hasUserLiked(deckList.id, it.id.toUuid()) }

            call.respond(deckList)
        }

        authenticate("auth-session") {
            post("/deck/new") {
                val session = call.authentication.principal<UserSession>()

                if (session == null) {
                    call.respondRedirect("/login")
                    return@post
                }

                val deck = call.receive<DeckRequest>()
                val createDeckResponse = deckRepo.createDeck(session.id.toUuid(), deck.name, deck.format, deck.visibility)
                call.respond(mapOf("hash" to createDeckResponse.hash))
            }

            get("/decks/personal") {
                val session = call.authentication.principal<UserSession>()

                if (session == null) {
                    call.respondRedirect("/login")
                    return@get
                }

                val decks = deckRepo.getUserDecks(session.id.toUuid())
                call.respond(decks)
            }

            post("/decks/import") {
                val session = call.authentication.principal<UserSession>()

                if (session == null) {
                    call.respondRedirect("/login")
                    return@post
                }

                val importedDecks = call.receive<DeckImportRequest>()

                val decks = deckRepo.importAll(session.id.toUuid(), importedDecks)
                call.respond(decks)
                call.respond(hashMapOf("test" to "to"))
            }

            put("/decks/{id}") {
                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    call.respondRedirect("/login")
                    return@put
                }

                val deckId = call.parameters["id"]?.toUuid() ?: return@put call.respond(HttpStatusCode.BadRequest)
                val settings = call.receive<DeckSettings>()

                val deckList = deckRepo.updateSettings(session.id.toUuid(), deckId, settings)
                if (deckList == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deck not found"))
                    return@put
                }
                call.respond(deckList)
            }
            
            patch("/decks/{id}") {
                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    call.respondRedirect("/login")
                    return@patch
                }

                val deckId = call.parameters["id"]?.toUuid() ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val patchSettings = call.receive<DeckPatchSettings>()

                val deckList = deckRepo.patchSettings(session.id.toUuid(), deckId, patchSettings)
                if (deckList == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deck not found"))
                    return@patch
                }
                call.respond(deckList)
            }

            delete("/decks/{id}") {
                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    call.respondRedirect("/login")
                    return@delete
                }

                val deckId = call.parameters["id"]?.toUuid() ?: return@delete call.respond(HttpStatusCode.BadRequest)

                deckRepo.deleteDeck(session.id.toUuid(), deckId)
                call.respond(hashMapOf("deleted" to deckId))
            }

            post ("/decks/{id}/edit") {

                val cookies = call.request.cookies.rawCookies

                log.debug("Received Cookies: {}", cookies)

                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    log.error("Authentication failed for edit route")
                    call.respondRedirect("/login")
                    return@post
                }

                val deckId = call.parameters["id"]?: return@post call.respond(HttpStatusCode.BadRequest)

                val editRequest = call.receive<DeckEditRequest>()
                val response = deckRepo.editDeck(session.id.toUuid(), deckId, editRequest.cardId, editRequest.count)
                call.respond(response!!)
            }

            post ("/decks/{id}/like") {

                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    log.error("Authentication failed for deck add like route")
                    call.respondRedirect("/login")
                    return@post
                }

                val deckId = call.parameters["id"].tryUuid() ?: return@post call.respond(HttpStatusCode.BadRequest)

                deckRepo.upsertLike(deckId, session.id.toUuid(), false)
                call.respond(HttpStatusCode.OK)

            }

            delete("/decks/{id}/like") {
                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    log.error("Authentication failed for deck remove like route")
                    call.respondRedirect("/login")
                    return@delete
                }

                val deckId = call.parameters["id"].tryUuid() ?: return@delete call.respond(HttpStatusCode.BadRequest)

                deckRepo.upsertLike(deckId, session.id.toUuid(), true)
                call.respond(HttpStatusCode.OK)

            }

            post ("/decks/{id}/favorite") {

                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    log.error("Authentication failed for deck add favorite route")
                    call.respondRedirect("/login")
                    return@post
                }

                val deckId = call.parameters["id"]?.tryUuid() ?: return@post call.respond(HttpStatusCode.BadRequest)

                deckRepo.upsertFavorite(deckId, session.id.toUuid(), false)

                call.respond(HttpStatusCode.OK)
            }

            delete("/decks/{id}/favorite") {
                val session = call.authentication.principal<UserSession>()
                if (session == null) {
                    log.error("Authentication failed for deck remove favorite route")
                    call.respondRedirect("/login")
                    return@delete
                }

                val deckId = call.parameters["id"]?.tryUuid() ?: return@delete call.respond(HttpStatusCode.BadRequest)

                deckRepo.upsertFavorite(deckId, session.id.toUuid(), true)
                call.respond(HttpStatusCode.OK)

            }
        }
    }
}