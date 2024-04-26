package app.herocraft.core.security

import app.herocraft.plugins.UserSession
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Application.registerSecurityRouter(userService: UserService){
    routing {
        post("/login") {
            val user = call.receive<LoginRequest>()

            val foundUser = userService.getUser(user.email, user.password)

            if (foundUser != null) {
                call.sessions.set(UserSession(foundUser.id.toString(), foundUser.email))
                call.respond(200)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid login credentials")
            }
        }

        authenticate("auth-session") {
            get ("/protected") {
                val principal = call.principal<UserSession>()
                val email = principal?.email
                call.respondText("Hello, $email!")
            }
        }

    }
}