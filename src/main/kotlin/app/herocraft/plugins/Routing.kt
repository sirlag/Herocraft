package app.herocraft.plugins

import app.herocraft.features.images.ImageProcessor
import app.herocraft.features.metrics.MetricService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    routing {

        val metricService by inject<MetricService>()

        get("/") {
            call.respondText("Hello World!")
        }

        get("/version") {
            call.respondText(metricService.versionString)
        }
    }
}
