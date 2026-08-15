package com.coffeecart.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL")
            ?: error("DATABASE_URL environment variable is not set")

        // DATABASE_URL comes as postgresql://user:password@host:port/db — JDBC needs jdbc:postgresql://host:port/db
        val withoutScheme = databaseUrl.removePrefix("postgresql://")
        val userInfo = withoutScheme.substringBefore("@").split(":")
        val hostAndDb = withoutScheme.substringAfter("@")

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$hostAndDb"
            username = userInfo[0]
            password = userInfo[1]
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.create(CoffeeCartsTable)
            // One-off cleanup: an earlier schema had a NOT NULL "is_open" column that no longer exists in code.
            exec("ALTER TABLE coffee_carts DROP COLUMN IF EXISTS is_open")
        }
    }
}
