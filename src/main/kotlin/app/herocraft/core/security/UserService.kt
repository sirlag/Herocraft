package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.models.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.crypt.Algorithms
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.*

class UserService(private val database: Database) {
    object Users : Table() {
        val id = uuid("id").autoGenerate()
        val username = text("username")
        val email = text("email")
        val password = encryptedVarchar("password", 80, Algorithms.AES_256_PBE_GCM("enchantress", "E3FA597D8D9C98E5"))

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    suspend fun create(user: UserRequest): UUID = dbQuery {
        Users.insert {
            it[username] = user.username
            it[email] = user.email
            it[password] = user.password
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
            Users.select(Users.id, Users.email, Users.username)
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
        )

}

