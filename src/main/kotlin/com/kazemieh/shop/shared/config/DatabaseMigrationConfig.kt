package com.kazemieh.shop.shared.config

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class DatabaseMigrationConfig {

    @Bean
    fun migrateDatabase(jdbcTemplate: JdbcTemplate) = CommandLineRunner {
        try {
            jdbcTemplate.execute("ALTER TABLE payments ALTER COLUMN order_id DROP NOT NULL;")
            println("Successfully dropped NOT NULL constraint on payments.order_id")
        } catch (e: Exception) {
            // Ignore if it fails (e.g. column already nullable or table doesn't exist yet)
            println("Note: Database migration for payments.order_id skipped or failed: ${e.message}")
        }
    }
}
