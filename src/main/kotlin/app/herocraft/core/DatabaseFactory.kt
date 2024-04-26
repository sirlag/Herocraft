package app.herocraft.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    fun init(url: String) {
        val datasource = hikari(url)
        val flyway = Flyway.configure().dataSource(datasource).load()
        flyway.migrate()
    }

    fun hikari(url: String): HikariDataSource {
        val config = with(HikariConfig()) {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = url
            username = "postgres"
            password = "password"
            maximumPoolSize = 30
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
            this
        }
        return HikariDataSource(config)
    }
}