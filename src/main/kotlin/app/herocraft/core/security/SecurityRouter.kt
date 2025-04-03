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
    notificationManager: NotificationManager
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
                notificationManager.sendVerificationEmail(registration.email, token)
                call.respond(200)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid registration.")
            }
        }

        get("account/verification/verify/{token}") {
            call.parameters["token"]?.let {
                val verified = verificationService.verify(it)
                when(verified) {
                    true -> call.respond(HttpStatusCode.OK)
                    false -> call.respond(HttpStatusCode.Unauthorized, "Invalid Verification Token")
                }
                return@get call.respond(HttpStatusCode.OK)
            }

            call.respond(HttpStatusCode.BadRequest, "No token found")
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

            get("account/verification/resend") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respondRedirect("/login")
                    return@get
                }

                val token = verificationService.create(principal.id.toUUID())
                notificationManager.sendVerificationEmail(principal.email, token)
            }
        }

    }
}