package com.mad.statistics.config

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties
import javax.sql.DataSource

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)
    
    fun init() {
        try {
            // Загрузка драйвера PostgreSQL
            Class.forName("org.postgresql.Driver")
            
            // Проверка соединения
            getConnection().use { connection ->
                val result = connection.createStatement().executeQuery("SELECT 1")
                if (result.next()) {
                    logger.info("Successfully connected to PostgreSQL")
                }
            }
            
            createTablesIfNotExist()
            
            logger.info("Database initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize database", e)
            throw e
        }
    }
    
    fun getConnection(): Connection {
        return DriverManager.getConnection(
            AppConfig.clickhouseUrl,
            AppConfig.clickhouseUser,
            AppConfig.clickhousePassword
        )
    }
    
    private fun createTablesIfNotExist() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                // Создание таблицы для GPS данных
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS gps_data (
                        id VARCHAR(255) PRIMARY KEY,
                        exercise_id VARCHAR(255) NOT NULL,
                        timestamp TIMESTAMP NOT NULL,
                        position_timestamp TIMESTAMP NOT NULL,
                        latitude DOUBLE PRECISION NOT NULL,
                        longitude DOUBLE PRECISION NOT NULL,
                        altitude DOUBLE PRECISION NOT NULL,
                        speed DOUBLE PRECISION NOT NULL,
                        accuracy DOUBLE PRECISION NOT NULL
                    )
                """.trimIndent())
                
                // Создание индекса для быстрого поиска по exercise_id
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_gps_exercise_id ON gps_data(exercise_id)
                """.trimIndent())
                
                // Создание таблицы для данных о сердечном ритме
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS heart_rate_data (
                        id VARCHAR(255) PRIMARY KEY,
                        exercise_id VARCHAR(255) NOT NULL,
                        timestamp TIMESTAMP NOT NULL,
                        bpm INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Создание индекса для быстрого поиска по exercise_id
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_heart_rate_exercise_id ON heart_rate_data(exercise_id)
                """.trimIndent())
                
                // Создание таблицы для данных о калориях
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS calories_data (
                        id VARCHAR(255) PRIMARY KEY,
                        user_id VARCHAR(255) NOT NULL,
                        timestamp TIMESTAMP NOT NULL,
                        calories DOUBLE PRECISION NOT NULL
                    )
                """.trimIndent())
                
                // Создание индекса для быстрого поиска по user_id
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_calories_user_id ON calories_data(user_id)
                """.trimIndent())
            }
        }
    }
}