package app.herocraft.core.security


import app.herocraft.core.extensions.DataService
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.encodeToByteArray
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.days

class VerificationService(database: Database): DataService(database) {
    object Verification : Table() {
        val id = uuid("id").autoGenerate()
        val token = text("token").uniqueIndex()
        val userId = reference(
            name = "user_id",
            refColumn = UserService.Users.id,
            onDelete = ReferenceOption.CASCADE
        ).index(isUnique = false)
        val createdAt = timestamp("created_at")
        val expiresAt = timestamp("expires_at")

        override val primaryKey = PrimaryKey(id)
    }

    private val logger = LoggerFactory.getLogger(Verification::class.java)!!

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

    suspend fun verify(token: String) = dbQuery {
        val now = Clock.System.now()

        val verifiedUserId = Verification
            .select(Verification.userId)
            .where { (Verification.token eq token) and (Verification.expiresAt greater now) }
            .map { it[Verification.userId] }
            .firstOrNull()

        if (verifiedUserId != null) {
            logger.debug("Found verification for user {}", verifiedUserId)
            val updateCount = UserService.Users.update ({ UserService.Users.id eq verifiedUserId }) {
                it[verified] = true
                it[verifiedAt] = now
                it[lastModified] = now
            }
            if (updateCount < 1) {
                logger.error("Unable to update verification for user $verifiedUserId")
                return@dbQuery false
            }
            return@dbQuery true
        } else {
            logger.error("Invalid verification token supplied")
            return@dbQuery false
        }
    }

}
