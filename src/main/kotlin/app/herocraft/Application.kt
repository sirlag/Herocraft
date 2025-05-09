package app.herocraft

import app.herocraft.core.security.UserRepo
import app.herocraft.core.security.UserService
import app.herocraft.core.security.registerSecurityRouter
import app.herocraft.features.builder.DeckRepo
import app.herocraft.features.builder.registerBuilder
import app.herocraft.features.images.ImageService
import app.herocraft.features.images.S3Processor
import app.herocraft.features.search.CardRepo
import app.herocraft.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    val userRepo:UserRepo by inject()
    val deckRepo: DeckRepo by inject()
    val userService: UserService by inject()

    configureSessions()
    configureSecurity()
    registerSecurityRouter(userRepo, userService)
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    registerBuilder(deckRepo)
    configureRouting()
    install(CORS) {
        allowHost("localhost:5173")
        allowHost("herocraft.app")
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Put)
    }
}
