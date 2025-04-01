package app.herocraft.core.services

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.UserService
import app.herocraft.core.security.VerificationService
import app.herocraft.features.builder.DeckService
import app.herocraft.features.notifications.NotificationManager
import app.herocraft.features.search.CardService
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

class Services(app: Application) {

    private val database: Database

    val deckService: DeckService
    val cardService: CardService
    val userService: UserService
    val verificationService: VerificationService

    val notificationManager: NotificationManager

    init {
        val jdbcUrl = app.environment.config.property("herocraft.db.postgres.url").getString()
        val datasource = DatabaseFactory.hikari(jdbcUrl)
        DatabaseFactory.init(datasource)

        database = Database.connect(datasource)

        cardService = CardService(database)
        verificationService = VerificationService(database)
        userService = UserService(database)
        deckService = DeckService(database, userService, cardService)

        notificationManager = NotificationManager(app.environment.config)
    }
}

fun Application.registerServices(): Services =
    Services(this)
