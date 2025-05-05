package com.mad.statistics.services

import com.example.datalogger.logger.ActivityLogger
import com.example.datalogger.logger.ErrorLogger
import com.example.datalogger.model.ActivityLogEvent
import com.example.datalogger.model.ErrorLogEvent
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Сервис для логирования активности и ошибок */
class LoggingService : KoinComponent {
  private val activityLogger: ActivityLogger by inject()
  private val errorLogger: ErrorLogger by inject()

  /** Логирует пользовательскую активность */
  fun logActivity(
      userId: String?,
      eventType: String,
      deviceModel: String? = null,
      details: Map<String, Any?> = emptyMap()
  ): ActivityLogEvent = runBlocking {
    activityLogger.logActivity(
        userId = userId, eventType = eventType, deviceModel = deviceModel, details = details)
  }

  /** Логирует ошибку */
  fun logError(
      userId: String?,
      eventType: String,
      errorMessage: String,
      deviceModel: String? = null,
      exception: Throwable? = null,
      details: Map<String, Any?> = emptyMap()
  ): ErrorLogEvent = runBlocking {
    errorLogger.logError(
        userId = userId,
        eventType = eventType,
        errorMessage = errorMessage,
        deviceModel = deviceModel,
        stackTrace = exception?.stackTraceToString(),
        previousActivities = emptyList() // Можно добавить получение предыдущих активностей
        )
  }
}
