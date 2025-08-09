package app.herocraft.di

import app.herocraft.core.DatabaseFactory
import app.herocraft.core.security.RedisSessionStorage
import app.herocraft.core.security.ResetTokenRepo
import app.herocraft.core.security.UserRepo
import app.herocraft.core.security.VerificationRepo
import app.herocraft.features.builder.DeckRepo
import app.herocraft.features.search.CardImageRepo
import app.herocraft.features.search.CardRepo
import io.ktor.server.application.*
import io.lettuce.core.RedisClient
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {

    single {

        val application: Application = get()

        val jdbcUrl = application.environment.config.property("herocraft.db.postgres.url").getString()
        val user = application.environment.config.property("herocraft.db.postgres.user").getString()
        val password = application.environment.config.property("herocraft.db.postgres.password").getString()

        Database.connect(DatabaseFactory.init(DatabaseFactory.hikari(jdbcUrl, user, password)))
    }

    singleOf(::CardRepo)
    singleOf(::CardImageRepo)
    singleOf(::DeckRepo)
    singleOf(::ResetTokenRepo)
    singleOf(::VerificationRepo)
    singleOf(::UserRepo)

    single {
        val application: Application = get()

        val redisUrl = application.environment.config.property("herocraft.db.redis.url").getString()
        RedisClient.create(redisUrl)
    }

    singleOf(::RedisSessionStorage)

}