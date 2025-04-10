package app.herocraft.core.services

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.ResetTokenRepo
import app.herocraft.core.security.UserRepo
import app.herocraft.core.security.UserService
import app.herocraft.core.security.VerificationRepo
import app.herocraft.features.builder.DeckRepo
import app.herocraft.features.notifications.NotificationManager
import app.herocraft.features.search.CardRepo
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

class Services(app: Application) {

    private val database: Database

    val cardRepo: CardRepo
    val deckRepo: DeckRepo
    val resetTokenRepo: ResetTokenRepo
    val userRepo: UserRepo
    val verificationRepo: VerificationRepo

    val notificationManager: NotificationManager

    val userService: UserService

    init {
        val jdbcUrl = app.environment.config.property("herocraft.db.postgres.url").getString()
        val datasource = DatabaseFactory.hikari(jdbcUrl)
        DatabaseFactory.init(datasource)

        database = Database.connect(datasource)

        cardRepo = CardRepo(database)
        resetTokenRepo = ResetTokenRepo(database)
        userRepo = UserRepo(database)
        verificationRepo = VerificationRepo(database)

        deckRepo = DeckRepo(database, userRepo, cardRepo)

        notificationManager = NotificationManager(app.environment.config)

        userService = UserService(userRepo, resetTokenRepo, notificationManager, verificationRepo)
    }
}

fun Application.registerServices(): Services =
    Services(this)
