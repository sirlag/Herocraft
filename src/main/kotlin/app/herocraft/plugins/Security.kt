package app.herocraft.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val id: String, val email: String)

fun Application.configureSecurity() {
    authentication {
        session<UserSession>("auth-session") {
            validate { session ->
                session
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}
