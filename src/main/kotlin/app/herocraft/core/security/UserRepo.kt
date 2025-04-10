package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.extensions.isEmailAddress
import app.herocraft.core.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.crypt.Algorithms
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepo(private val database: Database) {
    object Users : Table() {
        val id = uuid("id").autoGenerate()
        val username = text("username")
        val email = text("email")
        val password = encryptedVarchar("password", 80, Algorithms.AES_256_PBE_GCM("enchantress", "E3FA597D8D9C98E5"))
        val createdAt = timestamp("created_at")
        val lastModified = timestamp("last_modified")
        val verified = bool("verified").default(false)
        val verifiedAt = timestamp("verified_at").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    suspend fun create(user: UserRequest): UUID = dbQuery {
        val now = Clock.System.now()

        Users.insert {
            it[username] = user.username
            it[email] = user.email
            it[password] = user.password
            it[createdAt] = now
            it[lastModified] = now
            it[verified] = false
        }[Users.id].toKotlinUUID()
    }

    suspend fun getUser(username: String, password: String): User?
        = dbQuery {
            Users
                .selectAll()
                .where { Users.email eq username }
                .filter { it[Users.password] == password }
                .map { Users.fromResultRow(it) }
                .firstOrNull()

        }

    suspend fun getUser(id: UUID): User?
        = dbQuery {
            Users.select(Users.id, Users.email, Users.username, Users.verified)
                .where { Users.id eq id.toJavaUUID() }
                .map { Users.fromResultRow(it) }
                .firstOrNull()
    }

    suspend fun getUsername(id: UUID): String?
        = dbQuery {
            Users
                .select(Users.username)
                .where { Users.id eq id.toJavaUUID()}
                .map { it[Users.username] }
                .firstOrNull()
        }

//    suspend fun resetPassword(email: String) = dbQuery {
//        if (!email.isEmailAddress()) {
//            return@dbQuery
//        }
//
//        Users
//            .select(user)
//
//    }

//    suspend fun read(id: Int): User? {
//        return dbQuery {
//            Users.select { Users.id eq id }
//                .map { ExposedUser(it[Users.name], it[Users.age]) }
//                .singleOrNull()
//        }
//    }
//
//    suspend fun update(id: Int, user: User) {
//        dbQuery {
//            Users.update({ Users.id eq id }) {
//                it[name] = user.name
//                it[age] = user.age
//            }
//        }
//    }
//
//    suspend fun delete(id: Int) {
//        dbQuery {
//            Users.deleteWhere { Users.id.eq(id) }
//        }
//    }


    fun Users.fromResultRow(result: ResultRow): User =
        User(
            result[id].toKotlinUUID(),
            result[username],
            result[email],
            result[verified],
        )

}

