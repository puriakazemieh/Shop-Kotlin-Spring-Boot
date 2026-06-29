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
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN email DROP NOT NULL;")
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;")
            jdbcTemplate.execute("ALTER TABLE payments ALTER COLUMN order_id DROP NOT NULL;")
            // Security: registered users must default to CUSTOMER, not ADMIN.
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN role SET DEFAULT 'CUSTOMER';")
            println("Successfully updated database constraints for users and payments")
        } catch (e: Exception) {
            // Ignore if it fails (e.g. column already nullable or table doesn't exist yet)
            println("Note: Database migration skipped or failed: ${e.message}")
        }
    }
}
