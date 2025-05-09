package app.herocraft.core.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.sessions.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines

class RedisSessionStorage(private val client: RedisClient) : SessionStorage {

    private val logger = KotlinLogging.logger {}

    private val connection by lazy {
        client.connect().coroutines()
    }

    override suspend fun invalidate(id: String) {

        logger.trace { "Invalidated session with id $id" }

        connection.del(id)
    }

    override suspend fun read(id: String): String {
        val session = connection.get(id)

        logger.debug { "Fetched Session with id $id: ${session ?: "Session Not found."}" }
        return session ?: throw NoSuchElementException("No session $id")
    }

    override suspend fun write(id: String, value: String) {

        logger.trace { "Writing session with id $id" }

        if (connection.set(id, value) != "OK") throw IllegalStateException("Could not write session data")
    }

}