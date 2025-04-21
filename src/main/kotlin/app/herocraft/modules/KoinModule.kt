package app.herocraft.modules

import app.herocraft.di.databaseModule
import app.herocraft.di.serviceModule
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configureKoin() {
    log.info("Installing Koin plugin")
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single {this@configureKoin}
            },
            databaseModule, serviceModule)
    }
    log.info("Koin plugin installed and modules loaded.")
}