package app.herocraft

import app.herocraft.core.security.registerSecurityRouter
import app.herocraft.core.services.registerServices
import app.herocraft.features.builder.registerBuilder
import app.herocraft.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val services = registerServices()
    configureSessions()
    configureSecurity()
    registerSecurityRouter(services.userService)
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabases(services.userService, services.cardService)
    registerBuilder(services.deckService)
    configureRouting()
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
}
