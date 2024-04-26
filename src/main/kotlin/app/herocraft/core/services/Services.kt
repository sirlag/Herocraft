package app.herocraft.core.services

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.UserService
import app.herocraft.features.search.CardService
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

class Services(app: Application) {

    private val database: Database

    val userService: UserService
    val cardService: CardService

    init {
        val jdbcUrl = app.environment.config.property("herocraft.db.postgres.url").getString()
        val datasource = DatabaseFactory.hikari(jdbcUrl)
        DatabaseFactory.init(datasource)
        database = Database.connect(datasource)
        userService = UserService(database)
        cardService = CardService(database)
    }
}

fun Application.registerServices(): Services =
    Services(this)