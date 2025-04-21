package app.herocraft.plugins

import app.herocraft.core.security.RedisSessionStorage
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.koin.ktor.ext.inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Application.configureSessions() {
    val sessionStore: RedisSessionStorage by inject()

    install(Sessions) {

        val secretSignKey = hex("6819b57a326945c1968f45236589")

        cookie<UserSession>("user_session",  sessionStore){
            cookie.path = "/"
            cookie.maxAge = 7.toDuration(DurationUnit.DAYS)
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }
}