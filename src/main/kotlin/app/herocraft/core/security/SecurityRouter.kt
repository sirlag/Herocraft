package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.models.ErrorMessage
import app.herocraft.plugins.UserSession
import app.softwork.uuid.toUuid
import io.github.oshai.kotlinlogging.KotlinLogging
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
) {
    val logger = KotlinLogging.logger {}

    routing {

        post("/account/login") {
            val user = call.receive<LoginRequest>()

            val foundUser = userRepo.getUser(user.email, user.password)

            if (foundUser != null) {
                call.sessions.set(UserSession(foundUser.id.toString(), foundUser.email, foundUser.verified))
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized,
                    ErrorMessage("INVALID_LOGIN","Invalid login credentials")
                )
            }
        }

        post("/account/register") {
            val registration = call.receive<RegistrationRequest>()

            val userRequest = UserRequest(registration.username, registration.email, registration.password)
            val userId = userService.registerUser(userRequest)

            if (userId != null) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid registration.")
            }
        }

        post("/account/forgot") {
            val forgotRequest = try {
                call.receive<RequestPasswordResetRequest>()
            } catch (ex: ContentTransformationException) {
                logger.error { "Unable to properly transform request body: ${ex.message}" }
                logger.error { "Dumping request body: $call" }
                call.respond(HttpStatusCode.BadRequest, "Invalid request.")
                return@post
            }

            val user = userService.sendForgotPasswordEmail(forgotRequest.email)

            user.onSuccess {
                call.respond(HttpStatusCode.OK)
            }.onFailure {
                when (it) {
                    is UserNotFoundException ->
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorMessage("USER_NOT_FOUND", "Unable to find user registration")
                        )

                    is UserNotVerifiedExeception ->
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorMessage(
                                "USER_NOT_VERIFIED",
                                "Unable to send reset request for this email, please contact support"
                            )
                        )

                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorMessage.UnknownError
                    )
                }
            }
        }

        post("/account/reset") {
            val resetRequest = call.receive<PasswordResetRequest>()

            val user = userService.resetPassword(resetRequest)

            when (user) {
                true -> call.respond(HttpStatusCode.OK)
                false -> {
                    logger.error { "Failed to reset password" }
                    call.respond(HttpStatusCode.NotFound, "Unable to find associated user")
                }
            }
        }

        get("/account/verification/verify/{token}") {
            call.parameters["token"]?.let {
                val verified = userService.verifyEmail(it)
                when (verified) {
                    true -> call.respond(HttpStatusCode.OK)
                    false -> call.respond(HttpStatusCode.Unauthorized, "Invalid Verification Token")
                }
                return@get call.respond(HttpStatusCode.OK)
            }

            call.respond(HttpStatusCode.BadRequest, "No token found")
        }

        authenticate("auth-session") {

            post("/logout") {
                val principal = call.authentication.principal<UserSession>()
                if (principal != null) {
                    call.sessions.clear<UserSession>()
                    call.respond(HttpStatusCode.OK)
                }
            }

            get("/protected") {
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