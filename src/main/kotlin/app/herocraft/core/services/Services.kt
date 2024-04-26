package app.herocraft.core.services

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.UserService
import app.herocraft.features.search.CardService
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

class Services(app: Application) {

    private val database: Database

    val userService: UserService
    val cardService: CardService

    init {
        val jdbcUrl = app.environment.config.property("herocraft.db.postgres.url").getString()
        DatabaseFactory.init(jdbcUrl)
        database = Database.connect(DatabaseFactory.hikari(jdbcUrl))
        userService = UserService(database)
        cardService = CardService(database)
    }
}

fun Application.registerServices(): Services =
    Services(this)