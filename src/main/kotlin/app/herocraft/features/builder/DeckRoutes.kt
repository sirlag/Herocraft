package app.herocraft.features.builder

import app.herocraft.plugins.UserSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.uuid.toUUID

fun Application.registerBuilder(deckService: DeckService) {
    routing {
        authenticate("auth-session") {
            post("/deck/new") {
                val session = call.authentication.principal<UserSession>()

                if (session == null) {
                    call.respondRedirect("/login")
                    return@post
                }

                val deck = call.receive<DeckRequest>()
                val hash = deckService.createDeck(session.id.toUUID(), deck.name, deck.format, deck.visibility)
                call.respond(mapOf("hash" to hash))
            }

            get("/decks/personal") {
                val session = call.authentication.principal<UserSession>()

                if (session == null) {
                    call.respondRedirect("/login")
                    return@get
                }

                val decks = deckService.getUserDecks(session.id.toUUID())
                call.respond(decks)
            }
        }
    }
}