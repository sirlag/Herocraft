package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.User
import kotlinx.datetime.Clock
import org.jetbrains.exposed.crypt.Algorithms
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class UserRepo(private val database: Database) : DataService(database) {
    object Users : Table() {
        val id = uuid("id").autoGenerate()
        val username = text("username")
        val email = text("email")
        val password = encryptedVarchar("password", 80, Algorithms.AES_256_PBE_GCM("enchantress", "E3FA597D8D9C98E5"))
        val createdAt = timestamp("created_at")
        val lastModified = timestamp("last_modified")
        val verified = bool("verified").default(false)
        val verifiedAt = timestamp("verified_at").nullable()
        val active = bool("active").default(false)

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(user: UserRequest): Uuid = dbQuery {
        val now = Clock.System.now()

        Users.insert {
            it[username] = user.username
            it[email] = user.email
            it[password] = user.password
            it[createdAt] = now
            it[lastModified] = now
            it[verified] = false
            it[active] = true
        }[Users.id].toKotlinUuid()
    }

    suspend fun getUser(username: String, password: String): User? = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq username }
            .filter { it[Users.password] == password }
            .map { Users.fromResultRow(it) }
            .firstOrNull()

    }

    suspend fun getUser(id: Uuid): User? = dbQuery {
        Users.select(Users.id, Users.email, Users.username, Users.verified)
            .where { Users.id eq id.toJavaUuid() }
            .map { Users.fromResultRow(it) }
            .firstOrNull()
    }

    suspend fun getUserWithEmail(email: String): User? = dbQuery {
        Users.selectAll()
            .where { (Users.email eq email) and (Users.active eq true) }
            .map { Users.fromResultRow(it) }
            .firstOrNull()
    }

    suspend fun getUsername(id: Uuid): String? = dbQuery {
        Users
            .select(Users.username)
            .where { Users.id eq id.toJavaUuid() }
            .map { it[Users.username] }
            .firstOrNull()
    }

//    suspend fun resetPassword(email: String) = dbQuery {
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
            result[id].toKotlinUuid(),
            result[username],
            result[email],
            result[verified],
        )

}

