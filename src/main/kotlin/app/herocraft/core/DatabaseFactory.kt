package app.herocraft.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    fun init() {
        val datasource = hikari()
        Database.connect(datasource)
        val flyway = Flyway.configure().dataSource(datasource).load()
        flyway.migrate()
    }

    fun hikari(): HikariDataSource {
        val config = with(HikariConfig()) {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://localhost:5432/herocrafter"
            username = "postgres"
            password = "password"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
            this
        }
        return HikariDataSource(config)
    }
}