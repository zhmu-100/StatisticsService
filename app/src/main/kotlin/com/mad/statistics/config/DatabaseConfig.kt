package com.mad.statistics.config

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.client.LoggerClient
import com.mad.model.LogLevel
import kotlinx.coroutines.runBlocking

object DatabaseConfig {
    private val loggerClient = LoggerClient()

    private fun logInfo(message: String) {
        loggerClient.logActivity(event = message, level = LogLevel.INFO)
    }

    private fun logWarn(message: String) {
        loggerClient.logActivity(event = message, level = LogLevel.WARN)
    }

    private fun logError(message: String, e: Exception? = null) {
        if (e != null) {
            loggerClient.logError(
                event = message,
                errorMessage = e.message ?: "Unknown error",
                stackTrace = e.stackTraceToString()
            )
        } else {
            loggerClient.logActivity(event = message, level = LogLevel.ERROR)
        }
    }

    fun init() {
        try {
            val clickHouseServiceClient = ClickHouseServiceClient()

            logInfo("Initializing database connection in ${AppConfig.dbMode} mode")
            logInfo("Using ClickHouse Service URL: ${AppConfig.clickhouseServiceUrl}")

            runBlocking {
                val isConnected = clickHouseServiceClient.checkConnection()
                if (isConnected) {
                    logInfo("Successfully connected to ClickHouse Service")
                } else {
                    logWarn("Could not connect to ClickHouse Service")
                }
            }

            logInfo("Database initialized successfully with URL: ${AppConfig.clickhouseServiceUrl}")
        } catch (e: Exception) {
            logError("Error initializing database: ${e.message}", e)
        }
    }
}
