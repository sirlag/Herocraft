package app.herocraft.core.security


import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.encodeToByteArray
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.days

class VerificationService(private val database: Database) {
    object Verification : Table() {
        val id = uuid("id").autoGenerate()
        val token = text("token").uniqueIndex()
        val userId = reference(
            name = "user_id",
            refColumn = UserService.Users.id,
            onDelete = ReferenceOption.CASCADE
        ).index()
        val createdAt = timestamp("created_at")
        val expiresAt = timestamp("expires_at")

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    suspend fun create(refId: UUID):String  = dbQuery {
        val now = Clock.System.now()

        val uuid = UUID.generateUUID()
        val newToken = uuid.encodeToByteArray().encodeBase64()
            .replace("/", "")
            .replace("+", "")

        Verification.insert {
            it[userId] = refId.toJavaUUID()
            it[token] = newToken
            it[createdAt] = now
            it[expiresAt] = now.plus(2.days)
        }[Verification.token]
    }

}
