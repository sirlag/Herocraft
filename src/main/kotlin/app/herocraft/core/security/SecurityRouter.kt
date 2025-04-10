package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.plugins.UserSession
import app.softwork.uuid.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.registerSecurityRouter(
    userRepo: UserRepo,
    userService: UserService
){
    routing {

        post("/account/login") {
            val user = call.receive<LoginRequest>()

            val foundUser = userRepo.getUser(user.email, user.password)

            if (foundUser != null) {
                call.sessions.set(UserSession(foundUser.id.toString(), foundUser.email, foundUser.verified))
                call.respond(200)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid login credentials")
            }
        }

        post("/account/register") {
            val registration = call.receive<RegistrationRequest>()

            val userRequest = UserRequest(registration.username, registration.email, registration.password)
            val userId = userService.registerUser(userRequest)

            if (userId != null) {
                call.respond(200)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid registration.")
            }
        }

        post("/account/forgot") {
            val forgotRequest = call.receive<ResetPasswordRequest>()

            val user = userService.sendForgotPasswordEmail(forgotRequest.email)

            when(user) {
                true -> call.respond(HttpStatusCode.OK)
                false -> call.respond(HttpStatusCode.NotFound, "Unable to find user registration")
            }
        }

        get("/account/verification/verify/{token}") {
            call.parameters["token"]?.let {
                val verified = userService.verifyEmail(it)
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

                val user = userRepo.getUser(principal.id.toUuid())!!
                call.respond(HttpStatusCode.OK, user)
            }

            get("account/verification/resend") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respondRedirect("/login")
                    return@get
                }

                userService.sendEmailVerificationEmail(principal.id.toUuid(), principal.email)
            }
        }

    }
}