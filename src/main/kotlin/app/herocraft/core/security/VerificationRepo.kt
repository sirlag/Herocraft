package app.herocraft.core.security


import app.herocraft.core.extensions.DataService
import io.ktor.util.*
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class VerificationRepo(database: Database): DataService(database) {
    object Verification : Table() {
        val id = uuid("id").autoGenerate()
        val token = text("token").uniqueIndex()
        val userId = reference(
            name = "user_id",
            refColumn = UserRepo.Users.id,
            onDelete = ReferenceOption.CASCADE
        ).index(isUnique = false)
        val createdAt = timestamp("created_at")
        val expiresAt = timestamp("expires_at")

        override val primaryKey = PrimaryKey(id)
    }

    private val logger = LoggerFactory.getLogger(Verification::class.java)!!

    suspend fun create(refId: Uuid):String  = dbQuery {
        val now = Clock.System.now()

        val uuid = Uuid.random()
        val newToken = uuid.toByteArray().encodeBase64()
            .replace("/", "")
            .replace("+", "")

        Verification.insert {
            it[userId] = refId.toJavaUuid()
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
            val updateCount = UserRepo.Users.update ({ UserRepo.Users.id eq verifiedUserId }) {
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
