package app.herocraft.plugins

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.api.UserRequest
import app.herocraft.core.security.UserService
import app.herocraft.features.search.CardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.uuid.toUUIDOrNull
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases(userService: UserService, cardService: CardService) {
    routing {
        // Create user
        post("/users") {
            val user = call.receive<UserRequest>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

//        // Read user
//        get("/users/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            val user = userService.read(id)
//            if (user != null) {
//                call.respond(HttpStatusCode.OK, user)
//            } else {
//                call.respond(HttpStatusCode.NotFound)
//            }
//        }
//
//        // Update user
//        put("/users/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            val user = call.receive<ExposedUser>()
//            userService.update(id, user)
//            call.respond(HttpStatusCode.OK)
//        }
//
//        // Delete user
//        delete("/users/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            userService.delete(id)
//            call.respond(HttpStatusCode.OK)
//        }

        post("/cards/reload") {
            val results = cardService.resetTable()
            call.respond(HttpStatusCode.OK, results)
        }

        get("/cards") {
            val searchString = call.request.queryParameters["q"]

            var page = call.request.queryParameters["page"]?.toInt() ?: 1
            if (page == 0) {
                page = 1
            }

            println(call.request.queryString())
            val results = searchString
                ?.let { cardService.search(searchString, page=page) } ?: cardService.getPaging(page = page)
            call.respond(HttpStatusCode.OK, results)
        }

        get("card/{uuid}") {
            val uuid = call.parameters["uuid"]!!
            val card = uuid.toUUIDOrNull()?.let { cardService.getOne(it) }
            card?.let { call.respond(HttpStatusCode.OK, it) } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
