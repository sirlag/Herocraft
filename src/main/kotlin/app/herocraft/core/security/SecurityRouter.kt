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
                call.sessions.set(
                    UserSession(
                        id = foundUser.id.toString(),
                        email = foundUser.email,
                        verified = foundUser.verified,
                        username = foundUser.username,
                        displayName = foundUser.displayName
                    )
                )
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorMessage("INVALID_LOGIN", "Invalid login credentials")
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
            }

            call.respond(HttpStatusCode.BadRequest, "No token found")
        }

        authenticate("auth-session") {

            // Current user info endpoint for client apps
            get("/api/me") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                val user = userRepo.getUser(principal.id.toUuid())
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(
                    app.herocraft.core.api.UserInfo(
                        id = user.id,
                        username = user.username,
                        displayName = user.displayName,
                        email = user.email,
                        verified = user.verified,
                        isAdmin = false // will be updated once role checks are wired through repo
                    )
                )
            }

            // Update profile (display name)
            patch("/api/me/profile") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@patch
                }

                val body = try {
                    call.receive<ProfileUpdateRequest>()
                } catch (ex: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_BODY", "Invalid request body"))
                    return@patch
                }

                val newName = body.displayName.trim()
                if (newName.isEmpty() || newName.length > 32 || !newName.any { !it.isWhitespace() }) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorMessage("INVALID_DISPLAY_NAME", "Display name must be 1-32 characters and contain a non-blank character")
                    )
                    return@patch
                }

                val updated = userRepo.updateDisplayName(principal.id.toUuid(), newName)
                if (updated > 0) {
                    // refresh session with new displayName
                    call.sessions.set(
                        UserSession(
                            id = principal.id,
                            email = principal.email,
                            verified = principal.verified,
                            username = principal.username,
                            displayName = newName
                        )
                    )
                    call.respond(HttpStatusCode.OK, mapOf("displayName" to newName))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, ErrorMessage("UPDATE_FAILED", "Failed to update display name"))
                }
            }

            // Change password
            post("/api/me/security/password") {
                val principal = call.authentication.principal<UserSession>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val body = try {
                    call.receive<ChangePasswordRequest>()
                } catch (ex: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_BODY", "Invalid request body"))
                    return@post
                }

                val current = body.currentPassword.trim()
                val newPass = body.newPassword.trim()
                if (current.isEmpty() || newPass.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorMessage("INVALID_PASSWORD", "Both current and new password are required"))
                    return@post
                }

                val ok = userRepo.verifyPassword(principal.id.toUuid(), current)
                if (!ok) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorMessage("INVALID_CREDENTIALS", "Current password is incorrect"))
                    return@post
                }

                val updated = userRepo.changePassword(principal.id.toUuid(), newPass)
                if (updated > 0) {
                    // Refresh session (no secret fields in session)
                    call.sessions.set(
                        UserSession(
                            id = principal.id,
                            email = principal.email,
                            verified = principal.verified,
                            username = principal.username,
                            displayName = principal.displayName
                        )
                    )
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, ErrorMessage("UPDATE_FAILED", "Failed to update password"))
                }
            }

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
                call.respond(HttpStatusCode.OK, user.copy(isAdmin = false))
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