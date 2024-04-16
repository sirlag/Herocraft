package app.herocraft

import app.herocraft.core.DatabaseFactory
import app.herocraft.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    DatabaseFactory.init()
    configureDatabases()
    configureRouting()
    install(CORS) {
        anyHost()
    }
}