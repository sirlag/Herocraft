package app.herocraft.plugins

import app.herocraft.core.api.UserRequest
import app.herocraft.core.security.UserRepo
import app.herocraft.features.images.ImageProcessor
import app.herocraft.features.images.ImageService
import app.herocraft.features.images.S3Processor
import app.herocraft.features.search.CardImageRepo
import app.herocraft.features.search.CardRepo
import app.softwork.uuid.toUuidOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.Uuid

fun Application.configureDatabases(
    userRepo: UserRepo,
    cardRepo: CardRepo,
    imageService: ImageService,
    s3Processor: S3Processor
) {
    routing {
        // Create user
        post("/users") {
            val user = call.receive<UserRequest>()
            val id = userRepo.create(user)
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
            val results = cardRepo.resetTable()
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
                ?.let { cardRepo.search(searchString, page = page) } ?: cardRepo.getPaging(page = page)
            println(results.toString())
//            results.hasNext //synthetic call to populate
            println(Json.encodeToJsonElement(results))
            call.respond(HttpStatusCode.OK, results)
        }

        get("card/{uuid}") {
            val uuid = call.parameters["uuid"]!!
            val card = uuid.toUuidOrNull()?.let { cardRepo.getOne(it) }
            card?.let { call.respond(HttpStatusCode.OK, it) } ?: call.respond(HttpStatusCode.NotFound)
        }

        get("/testing/process-images") {

            val cards = cardRepo.getAll()

            imageService.processAll(cards)

            call.respond(200)
        }

        get("/testing/test-s3-connection") {
            s3Processor.testConnection()
        }

    }
}
