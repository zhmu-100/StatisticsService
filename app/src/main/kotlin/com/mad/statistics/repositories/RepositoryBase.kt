package com.mad.statistics.repositories

import com.mad.client.LoggerClient
import com.mad.model.LogLevel
import com.mad.statistics.clients.ClickHouseServiceClient

abstract class RepositoryBase(protected val clickHouseServiceClient: ClickHouseServiceClient) {
    protected val loggerClient = LoggerClient()
    
    // Методы-обертки для совместимости с существующим кодом
    protected fun logInfo(message: String) {
        loggerClient.logActivity(event = message, level = LogLevel.INFO)
    }
    
    protected fun logWarn(message: String) {
        loggerClient.logActivity(event = message, level = LogLevel.WARN)
    }
    
    protected fun logError(message: String, e: Exception? = null) {
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
}