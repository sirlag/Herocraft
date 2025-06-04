package app.herocraft

import app.herocraft.core.extensions.toShortString
import app.herocraft.core.extensions.toUuidFromShort
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.uuid.Uuid

class UuidTests {

    @Test
    fun uuidTest() {
        val uuid = Uuid.random()

        val shortString = uuid.toShortString()

        val uuid2 = shortString.toUuidFromShort()
        assertEquals(uuid, uuid2)
    }
}