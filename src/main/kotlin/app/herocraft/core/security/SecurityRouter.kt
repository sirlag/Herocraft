package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.features.notifications.NotificationManager
import app.herocraft.plugins.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.uuid.toUUID

fun Application.registerSecurityRouter(
    userService: UserService,
    verificationService: VerificationService,
    notifcationManager: NotificationManager
){
    routing {
        post("/login") {
            val user = call.receive<LoginRequest>()

            val foundUser = userService.getUser(user.email, user.password)

            if (foundUser != null) {
                call.sessions.set(UserSession(foundUser.id.toString(), foundUser.email, foundUser.verified))
                call.respond(200)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid login credentials")
            }
        }

        post("/register") {
            val registration = call.receive<RegistrationRequest>()

            val user = userService.create(UserRequest(registration.username, registration.email, registration.password))

            if (user != null) {
                val token = verificationService.create(user)
                notifcationManager.sendVerificationEmail(registration.email, token)
                call.respond(200)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid registration.")
            }
        }

        authenticate("auth-session") {

            post( "/logout") {
                val principal = call.authentication.principal<UserSession>()
                if (principal != null) {
                    call.sessions.clear<UserSession>()
                    call.respond(200)
                }
            }

            get ("/protected") {
                val principal = call.authentication.principal<UserSession>()
                val email = principal?.email
                call.respondText("Hello, $email!")
            }

            get("/user") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respondRedirect("/login")
                    return@get
                }

                val user = userService.getUser(principal.id.toUUID())!!
                call.respond(HttpStatusCode.OK, user)
            }

            get("/resend") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respondRedirect("/login")
                    return@get
                }

                val token = verificationService.create(principal.id.toUUID())
                notifcationManager.sendVerificationEmail(principal.email, token)
            }
        }

    }
}