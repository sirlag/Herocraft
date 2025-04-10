package app.herocraft.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object DatabaseFactory {

    fun init(dataSource: DataSource) {
        val flyway = Flyway.configure().dataSource(dataSource).load()
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