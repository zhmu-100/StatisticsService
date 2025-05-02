package com.mad.statistics.plugins

import com.mad.statistics.services.LoggingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

/** Настраивает перехватчик ошибок с логированием */
fun Application.configureErrorHandling() {
  val loggingService: LoggingService by inject()

  install(StatusPages) {
    exception<Throwable> { call, cause ->
      // Получаем информацию о запросе
      val userId = call.request.headers["X-User-Id"]
      val deviceModel = call.request.headers["User-Agent"]
      val path = call.request.path()
      val method = call.request.httpMethod.value

      // Логируем ошибку
      loggingService.logError(
          userId = userId,
          eventType = "API_ERROR",
          errorMessage = cause.message ?: "Unknown error",
          deviceModel = deviceModel,
          exception = cause,
          details =
              mapOf(
                  "path" to path,
                  "method" to method,
                  "queryParameters" to call.request.queryParameters.entries().joinToString(),
                  "timestamp" to System.currentTimeMillis()))

      // Отправляем ответ клиенту
      call.respondText(text = "500: ${cause.message}", status = HttpStatusCode.InternalServerError)
    }
  }
}
