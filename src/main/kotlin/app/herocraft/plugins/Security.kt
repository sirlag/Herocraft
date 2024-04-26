package app.herocraft.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

data class UserSession(val id: String, val email: String) : Principal

fun Application.configureSecurity() {
    authentication {
        session<UserSession>("auth-session") {
            validate { session ->
                if (session.id != null) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}
