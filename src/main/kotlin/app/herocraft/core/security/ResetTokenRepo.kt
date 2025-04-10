package app.herocraft.core.security

import app.herocraft.core.extensions.DataService
import app.herocraft.core.security.UserRepo.Users
import kotlinx.datetime.Clock
import org.jetbrains.exposed.crypt.Algorithms
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.kotlincrypto.random.CryptoRand
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class ResetTokenRepo(database: Database): DataService(database) {
    object ResetToken : Table("resettokens") {
        val id = uuid("id").autoGenerate()
        val token = encryptedVarchar("token", 80, Algorithms.AES_256_PBE_GCM("enchantress", "E3FA597D8D9C98E5"))
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
}