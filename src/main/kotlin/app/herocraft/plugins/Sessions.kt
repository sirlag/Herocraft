package app.herocraft.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Application.configureSessions() {
    install(Sessions) {
        val secretSignKey = hex("6819b57a326945c1968f45236589")

        cookie<UserSession>("user_session", SessionStorageMemory()) {
            cookie.path = "/"
            cookie.maxAge = 30.toDuration(DurationUnit.MINUTES)
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }
}