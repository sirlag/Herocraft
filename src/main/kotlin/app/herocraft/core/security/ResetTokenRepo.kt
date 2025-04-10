package app.herocraft.core.security

import app.herocraft.core.extensions.DataService
import app.herocraft.core.security.UserRepo.Users
import io.ktor.server.engine.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.crypt.Algorithms
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.kotlincrypto.random.CryptoRand
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class ResetTokenRepo(database: Database): DataService(database) {

    val logger = LoggerFactory.getLogger(ResetTokenRepo::class.java)


    object ResetToken : Table("resettokens") {
        private val salt = applicationEnvironment().config.propertyOrNull("herocraft.db.salt")?.toString()
            ?: "CHANGETHIS"

        val id = uuid("id").autoGenerate()
        val token = encryptedVarchar("token", 80, Algorithms.BLOW_FISH(salt))
        val userId = reference(
            name = "user_id",
            refColumn = Users.id,
            onUpdate = ReferenceOption.CASCADE,
        ).index(isUnique = false)
        val createdAt = timestamp("created_at")
        val expiresAt = timestamp("expires_at")

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(userId: Uuid): String = dbQuery {
        val token = CryptoRand.Default.nextBytes(ByteArray(16)).toHexString(HexFormat.UpperCase)

        val now = Clock.System.now();
        val submittedToken = ResetToken.insert {
            it[this.token] = token
            it[this.userId] = userId.toJavaUuid()
            it[this.createdAt] = now
            it[this.expiresAt] = now + 1.hours
        }[ResetToken.token]

        return@dbQuery submittedToken
    }

    suspend fun consumeToken(token: String) = dbQuery {

        val now = Clock.System.now()

        ResetToken.deleteReturning(
            returning = listOf(ResetToken.userId),
            where = { (ResetToken.token eq token) and (ResetToken.expiresAt greater now) }
        )
            .map { it[ResetToken.userId] }.firstOrNull()?.toKotlinUuid()
    }
}