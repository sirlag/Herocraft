package app.herocraft.core.security

import io.ktor.server.sessions.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines

class RedisSessionStorage(private val client: RedisClient): SessionStorage {

    private val connection by lazy {
        client.connect().coroutines()
    }

    override suspend fun invalidate(id: String) {
        connection.del(id)
    }

    override suspend fun read(id: String): String {
        return connection.get(id) ?: throw NoSuchElementException("No session $id")
    }

    override suspend fun write(id: String, value: String) {
        connection.set(id, value)
    }

}