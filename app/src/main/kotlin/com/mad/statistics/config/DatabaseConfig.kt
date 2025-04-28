package com.mad.statistics.config

import com.mad.statistics.clients.ClickHouseServiceClient
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)
    
    fun init() {
        try {
            val clickHouseServiceClient = ClickHouseServiceClient()
            
            logger.info("Initializing database connection in ${AppConfig.dbMode} mode")
            logger.info("Using ClickHouse Service URL: ${AppConfig.clickhouseServiceUrl}")
            
            // Проверяем подключение к ClickHouse Service
            runBlocking {
                val isConnected = clickHouseServiceClient.checkConnection()
                if (isConnected) {
                    logger.info("Successfully connected to ClickHouse Service")
                } else {
                    logger.warn("Could not connect to ClickHouse Service")
                }
            }
            
            logger.info("Database initialized successfully with URL: ${AppConfig.clickhouseServiceUrl}")
        } catch (e: Exception) {
            logger.error("Error initializing database: ${e.message}", e)
        }
    }
}