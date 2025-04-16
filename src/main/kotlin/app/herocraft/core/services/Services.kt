package app.herocraft.core.services

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.*
import app.herocraft.features.builder.DeckRepo
import app.herocraft.features.images.ImageProcessor
import app.herocraft.features.images.ImageService
import app.herocraft.features.images.S3Processor
import app.herocraft.features.notifications.NotificationManager
import app.herocraft.features.search.CardImageRepo
import app.herocraft.features.search.CardRepo
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.lettuce.core.RedisClient
import org.jetbrains.exposed.sql.Database

class Services(app: Application) {

    private val database: Database

    val cardRepo: CardRepo
    val cardImageRepo: CardImageRepo
    val deckRepo: DeckRepo
    val resetTokenRepo: ResetTokenRepo
    val userRepo: UserRepo
    val verificationRepo: VerificationRepo

    val notificationManager: NotificationManager

    val userService: UserService
    val imageService: ImageService

    val sessionStorage: SessionStorage
    val s3Processor: S3Processor

    init {

        val config = app.environment.config

        // Database
        val jdbcUrl = config.property("herocraft.db.postgres.url").getString()
        val datasource = DatabaseFactory.hikari(jdbcUrl)
        DatabaseFactory.init(datasource)

        database = Database.connect(datasource)

        cardRepo = CardRepo(database)
        cardImageRepo = CardImageRepo(database)
        resetTokenRepo = ResetTokenRepo(database)
        userRepo = UserRepo(database)
        verificationRepo = VerificationRepo(database)

        deckRepo = DeckRepo(database, userRepo, cardRepo)

        val lettuce = RedisClient.create("redis://localhost")
        sessionStorage = RedisSessionStorage(lettuce)

        notificationManager = NotificationManager(config)

        val imageProcessor = ImageProcessor()

        val s3CredentialsProvider =  StaticCredentialsProvider() {
            accountId = config.property("herocraft.s3.id").getString()
            accessKeyId = config.property("herocraft.s3.key").getString()
            secretAccessKey = config.property("herocraft.s3.secret").getString()
        }

        val baseUrl = config.property("herocraft.s3.baseUrl").getString()
        val endpointUrl = config.property("herocraft.s3.endpoint").getString()


        s3Processor = S3Processor(s3CredentialsProvider, baseUrl, endpointUrl)

        imageService = ImageService(imageProcessor, s3Processor, cardImageRepo)
        userService = UserService(userRepo, resetTokenRepo, notificationManager, verificationRepo)


    }
}

fun Application.registerServices(): Services =
    Services(this)
