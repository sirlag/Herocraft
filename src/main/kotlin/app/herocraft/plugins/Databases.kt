package app.herocraft.plugins

import app.herocraft.core.api.UserRequest
import app.herocraft.core.security.UserRepo
import app.herocraft.features.images.ImageService
import app.herocraft.features.images.S3Processor
import app.herocraft.features.search.CardRepo
import app.herocraft.features.search.SearchService
import app.softwork.uuid.toUuidOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureDatabases() {

    val userRepo by inject<UserRepo>()
    val cardRepo by inject<CardRepo>()
    val imageService by inject<ImageService>()
    val searchService by inject<SearchService>()
    val s3Processor by inject<S3Processor>()

    routing {
        // Create user
        post("/users") {
            val user = call.receive<UserRequest>()
            val id = userRepo.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

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

            // Filter parameters
            val classes = call.parameters.getAll("c")
            val specs = call.parameters.getAll("s")
            val types = call.parameters.getAll("f")

            // Sort parameter
            val sortBy = call.request.queryParameters["sort"]

            log.info("Search string: $searchString, page: $page, sort: $sortBy")
            val results = searchService.search(searchString, classes, specs, types, page, sortBy)

            // results.hasNext //synthetic call to populate
            call.respond(HttpStatusCode.OK, results)
        }


        get("card/{uuid}") {
            val uuid = call.parameters["uuid"]!!
            val card = uuid.toUuidOrNull()?.let { cardRepo.getOne(it) }
            card?.let { call.respond(HttpStatusCode.OK, it) } ?: call.respond(HttpStatusCode.NotFound)
        }

        route("/admin/cards") {
            // List all cards (simple paging could be added later)
            get {
                val cards = cardRepo.getAll()
                call.respond(HttpStatusCode.OK, cards)
            }

            // Create a new card
            post {
                val card = call.receive<app.herocraft.core.models.IvionCard>()
                val created = cardRepo.create(card)
                call.respond(HttpStatusCode.Created, created)
            }

            // Read one
            get("/{id}") {
                val id = call.parameters["id"]?.toUuidOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid id")
                } else {
                    val one = cardRepo.getOne(id)
                    call.respond(HttpStatusCode.OK, one)
                }
            }

            // Update
            put("/{id}") {
                val id = call.parameters["id"]?.toUuidOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid id")
                } else {
                    val card = call.receive<app.herocraft.core.models.IvionCard>()
                    val updated = cardRepo.update(id, card)
                    call.respond(HttpStatusCode.OK, updated)
                }
            }

            // Delete
            delete("/{id}") {
                val id = call.parameters["id"]?.toUuidOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid id")
                } else {
                    val deleted = cardRepo.delete(id)
                    if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        get("/testing/process-images") {

            val cards = cardRepo.getAll()

            imageService.processAll(cards)

            call.respond(200)
        }

        get("/testing/test-s3-connection") {
            s3Processor.testConnection()

            call.respond(200)
        }

    }
}
