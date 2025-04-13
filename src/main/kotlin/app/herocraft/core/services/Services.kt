package app.herocraft.core.services

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.*
import app.herocraft.features.builder.DeckRepo
import app.herocraft.features.notifications.NotificationManager
import app.herocraft.features.search.CardRepo
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.lettuce.core.RedisClient
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

    val sessionStorage: SessionStorage

    init {

        val config = app.environment.config

        // Database
        val jdbcUrl = config.property("herocraft.db.postgres.url").getString()
        val datasource = DatabaseFactory.hikari(jdbcUrl)
        DatabaseFactory.init(datasource)

        database = Database.connect(datasource)

        cardRepo = CardRepo(database)
        resetTokenRepo = ResetTokenRepo(database)
        userRepo = UserRepo(database)
        verificationRepo = VerificationRepo(database)

        deckRepo = DeckRepo(database, userRepo, cardRepo)

        val lettuce = RedisClient.create("redis://localhost")
        sessionStorage = RedisSessionStorage(lettuce)

        notificationManager = NotificationManager(config)

        userService = UserService(userRepo, resetTokenRepo, notificationManager, verificationRepo)
    }
}

fun Application.registerServices(): Services =
    Services(this)
