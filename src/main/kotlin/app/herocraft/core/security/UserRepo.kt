package app.herocraft.core.security

import app.herocraft.core.api.UserRequest
import app.herocraft.core.extensions.DataService
import app.herocraft.core.models.User
import io.ktor.server.engine.*
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.crypt.Algorithms
import org.jetbrains.exposed.v1.crypt.encryptedVarchar
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class UserRepo(database: Database) : DataService(database) {
    object Users : Table() {
        private val salt = applicationEnvironment().config.propertyOrNull("herocraft.db.salt")?.toString()
            ?: "CHANGETHIS"

        val id = uuid("id").autoGenerate()
        val username = text("username")
        val displayName = text("display_name")
        val email = text("email")
        val password = encryptedVarchar("password", 80, Algorithms.BLOW_FISH(this.salt))
        val createdAt = timestamp("created_at")
        val lastModified = timestamp("last_modified")
        val verified = bool("verified").default(false)
        val verifiedAt = timestamp("verified_at").nullable()
        val active = bool("active").default(false)
        // Tracks when a user last changed their username (nullable until feature is implemented)
        val lastUsernameChangedAt = timestamp("last_username_changed_at").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    // Basic role system (matches Flyway V18 schema)
    object Roles : Table("role") {
        val id = uuid("id").autoGenerate()
        val name = text("name")
        val admin = bool("admin").default(false)
        val moderation = bool("moderation").default(false)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

        override val primaryKey = PrimaryKey(id)
    }

    object UserRoles : Table("user_role") {
        val userId = reference("user_id", Users.id)
        val roleId = reference("role_id", Roles.id)
        val assignedAt = timestamp("assigned_at")

        override val primaryKey = PrimaryKey(userId, roleId)
    }

    suspend fun create(user: UserRequest): Uuid = dbQuery {
        val now = Clock.System.now()

        Users.insert {
            it[username] = user.username
            it[displayName] = user.username // default displayName equals username
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
            .where { Users.email eq username and (Users.password eq password) }
            .map { Users.fromResultRow(it) }
            .firstOrNull()
    }

    suspend fun getUser(id: Uuid): User? = dbQuery {
        Users.selectAll()
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

    suspend fun changePassword(userId: Uuid, newPassword: String): Int = dbQuery {
        val now = Clock.System.now()
        return@dbQuery Users
            .update(
                where = { Users.id eq userId.toJavaUuid() and (Users.active eq true) }
            ) {
                it[password] = newPassword
                it[lastModified] = now
            }
    }

    suspend fun updateDisplayName(userId: Uuid, newDisplayName: String): Int = dbQuery {
        val now = Clock.System.now()
        return@dbQuery Users.update(
            where = { Users.id eq userId.toJavaUuid() and (Users.active eq true) }
        ) {
            it[displayName] = newDisplayName
            it[lastModified] = now
        }
    }

    suspend fun verifyPassword(userId: Uuid, candidate: String): Boolean = dbQuery {
        Users
            .selectAll()
            .where { (Users.id eq userId.toJavaUuid()) and (Users.password eq candidate) and (Users.active eq true) }
            .any()
    }

    // ---- Roles & Permissions ----
    suspend fun isAdmin(userId: Uuid): Boolean = dbQuery {
        (UserRoles innerJoin Roles)
            .selectAll()
            .where { (UserRoles.userId eq userId.toJavaUuid()) and (Roles.admin eq true) }
            .any()
    }

    suspend fun hasRole(userId: Uuid, roleName: String): Boolean = dbQuery {
        (UserRoles innerJoin Roles)
            .selectAll()
            .where { (UserRoles.userId eq userId.toJavaUuid()) and (Roles.name eq roleName) }
            .any()
    }

    suspend fun grantRole(userId: Uuid, roleName: String): Int = dbQuery {
        // Find role id by name
        val roleId = Roles
            .selectAll()
            .where { Roles.name eq roleName }
            .map { it[Roles.id] }
            .firstOrNull() ?: return@dbQuery 0

        // Check if already has it
        val exists = UserRoles
            .selectAll()
            .where { (UserRoles.userId eq userId.toJavaUuid()) and (UserRoles.roleId eq roleId) }
            .any()
        if (exists) return@dbQuery 0

        UserRoles.insert {
            it[this.userId] = userId.toJavaUuid()
            it[this.roleId] = roleId
            it[this.assignedAt] = Clock.System.now()
        }
        1
    }

    suspend fun revokeRole(userId: Uuid, roleName: String): Int = dbQuery {
        // Find role id by name
        val roleId = Roles
            .selectAll()
            .where { Roles.name eq roleName }
            .map { it[Roles.id] }
            .firstOrNull() ?: return@dbQuery 0

        UserRoles.deleteWhere { (UserRoles.userId eq userId.toJavaUuid()) and (UserRoles.roleId eq roleId) }
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


    private fun Users.fromResultRow(result: ResultRow): User =
        User(
            result[id].toKotlinUuid(),
            result[username],
            result[displayName],
            result[email],
            result[verified],
        )

    // Role membership queries were added above

}

