package app.herocraft.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val id: String,
    val email: String,
    val verified: Boolean,
    val username: String,
    val displayName: String
)

fun Application.configureSecurity() {

    val logger = KotlinLogging.logger {}

    authentication {
        session<UserSession>("auth-session") {
            validate { session ->
                logger.debug { "Validation user session: id = ${session.id}, email = ${session.email}, verified = ${session.verified}" }
                session
            }
            challenge {
                logger.debug { "Something has gone wrong. $it, cookies = ${call.request.cookies.rawCookies}"}
                call.respondRedirect("/login")
            }
        }
    }
}
