package com.mad.statistics.routes

import com.mad.client.LoggerClient
import com.mad.statistics.models.CaloriesData
import com.mad.statistics.services.CaloriesService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureCaloriesRoutes() {
  val caloriesService: CaloriesService by inject()

  // Используем внедрение зависимостей для LoggerClient
  val logger by inject<LoggerClient>()

  routing {
    route("/api/statistics") {
      // Получение данных о калориях по ID пользователя
      get("/calories") {
        val userId =
            call.request.queryParameters["user_id"]
                ?: return@get call.respondText(
                    "Missing user_id parameter", status = HttpStatusCode.BadRequest)

        try {
          // Логируем запрос данных о калориях
          logger.logActivity(
              event = "Calories data requested",
              userId = userId, // Используем user_id из запроса
              additionalData = mapOf("user_id" to userId))

          val caloriesData = caloriesService.getCaloriesDataByUserId(userId)

          // Логируем успешное получение данных
          logger.logActivity(
              event = "Calories data retrieved",
              userId = userId,
              additionalData =
                  mapOf("user_id" to userId, "records_count" to caloriesData.size.toString()))

          call.respond(mapOf("calories_data" to caloriesData))
        } catch (e: Exception) {
          // Логируем ошибку при получении данных
          logger.logError(
              event = "Calories data retrieval failed",
              errorMessage = e.message ?: "Unknown error",
              userId = userId,
              stackTrace = e.stackTraceToString())

          call.respondText(
              "Error retrieving calories data: ${e.message}",
              status = HttpStatusCode.InternalServerError)
        }
      }

      // Загрузка данных о калориях
      post("/calories") {
        try {
          val caloriesData = call.receive<CaloriesData>()

          // Создаем карту с информацией о данных калорий
          val logData = mutableMapOf<String, String>()

          // Безопасно добавляем данные
          try {
            // Добавляем данные из meta
            caloriesData.meta?.let { meta ->
              logData["user_id"] = meta.userId
              logData["timestamp"] = meta.timestamp.toString()
              logData["id"] = meta.id
            }

            // Добавляем информацию о калориях
            logData["calories"] = caloriesData.calories.toString()
          } catch (e: Exception) {
            logData["data_available"] = "false"
          }

          // Логируем загрузку данных о калориях
          logger.logActivity(
              event = "Calories data upload",
              userId = caloriesData.meta?.userId ?: call.request.headers["User-Id"],
              additionalData = logData)

          caloriesService.saveCaloriesData(caloriesData)
          call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
          // Логируем ошибку при загрузке данных
          logger.logError(
              event = "Calories data upload failed",
              errorMessage = e.message ?: "Unknown error",
              userId = call.request.headers["User-Id"],
              stackTrace = e.stackTraceToString())

          call.respondText(
              "Error uploading calories data: ${e.message}",
              status = HttpStatusCode.InternalServerError)
        }
      }
    }
  }
}
