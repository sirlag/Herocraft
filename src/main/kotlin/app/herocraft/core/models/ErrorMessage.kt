package app.herocraft.core.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(
    val error: String,
    val message: String,
) {
    companion object {
        val UnknownError = ErrorMessage("UNKNOWN_ERROR", "An unexpected error occurred")
    }

//
//    constructor(throwable: Throwable)  {
//        when (throwable) {
//            is UserNotFoundException -> ErrorMessage("USER_NOT_FOUND", "", status = HttpStatusCode.NotFound)
//        }
//    }

}

//                when (it) {
//                    is UserNotFoundException ->
//                        call.respond(HttpStatusCode.NotFound, "Unable to find user registration")
//
//                    is UserNotVerifiedExeception ->
//                        call.respond(
//                            HttpStatusCode.Unauthorized,
//                            "Unable to send reset request for this email, please contact support"
//                        )
//
//                    else -> call.respond(HttpStatusCode.InternalServerError, "An Unexpected error occurred")