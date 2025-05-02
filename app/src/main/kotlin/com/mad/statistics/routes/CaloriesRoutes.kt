package com.mad.statistics.routes

import com.mad.statistics.models.CaloriesData
import com.mad.statistics.services.CaloriesService
import com.mad.statistics.services.LoggingService // Добавлен импорт LoggingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureCaloriesRoutes() {
    val caloriesService: CaloriesService by inject()
    val loggingService: LoggingService by inject() // Добавлена инъекция LoggingService
    
    routing {
        
        route("/api/statistics") {
            // Получение данных о калориях по ID пользователя
            get("/calories") {
                val userId = call.request.queryParameters["user_id"]
                    ?: return@get call.respondText(
                        "Missing user_id parameter",
                        status = HttpStatusCode.BadRequest
                    )
                
                try {
                    // Логируем запрос на получение данных о калориях
                    loggingService.logActivity(
                        userId = userId,
                        eventType = "CALORIES_DATA_REQUEST",
                        deviceModel = call.request.headers["User-Agent"],
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            "path" to call.request.path(),
                            "queryParams" to call.request.queryParameters.entries().joinToString()
                        )
                    )
                    
                    val caloriesData = caloriesService.getCaloriesDataByUserId(userId)
                    
                    // Логируем успешный ответ
                    loggingService.logActivity(
                        userId = userId,
                        eventType = "CALORIES_DATA_RESPONSE",
                        deviceModel = call.request.headers["User-Agent"],
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            "dataPoints" to caloriesData.size,
                            "path" to call.request.path()
                        )
                    )
                    
                    call.respond(mapOf("calories_data" to caloriesData))
                } catch (e: Exception) {
                    // Логируем ошибку
                    loggingService.logError(
                        userId = userId,
                        eventType = "CALORIES_DATA_ERROR",
                        errorMessage = "Error retrieving calories data: ${e.message}",
                        deviceModel = call.request.headers["User-Agent"],
                        exception = e,
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            "path" to call.request.path()
                        )
                    )
                    
                    call.respondText(
                        "Error retrieving calories data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            
            // Загрузка данных о калориях
            post("/calories") {
                try {
                    val caloriesData = call.receive<CaloriesData>()
                    // Получаем userId из заголовка, так как в CaloriesData его нет
                    val userId = call.request.headers["X-User-Id"] ?: "unknown"
                    
                    // Логируем запрос на сохранение данных о калориях
                    loggingService.logActivity(
                        userId = userId,
                        eventType = "CALORIES_DATA_SAVE",
                        deviceModel = call.request.headers["User-Agent"],
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            // Используем toString() для получения информации о данных
                            "caloriesData" to caloriesData.toString(),
                            "path" to call.request.path()
                        )
                    )
                    
                    caloriesService.saveCaloriesData(caloriesData)
                    
                    // Логируем успешное сохранение
                    loggingService.logActivity(
                        userId = userId,
                        eventType = "CALORIES_DATA_SAVED",
                        deviceModel = call.request.headers["User-Agent"],
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            // Используем toString() для получения информации о данных
                            "caloriesData" to caloriesData.toString(),
                            "path" to call.request.path()
                        )
                    )
                    
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    // Получаем userId из заголовка
                    val userId = call.request.headers["X-User-Id"] ?: "unknown"
                    
                    // Логируем ошибку
                    loggingService.logError(
                        userId = userId,
                        eventType = "CALORIES_DATA_SAVE_ERROR",
                        errorMessage = "Error uploading calories data: ${e.message}",
                        deviceModel = call.request.headers["User-Agent"],
                        exception = e,
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            "path" to call.request.path()
                        )
                    )
                    
                    call.respondText(
                        "Error uploading calories data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
        route("/api/test") {
            get("/log") {
                val userId = call.request.headers["X-User-Id"] ?: "test-user"
                
                try {
                    // Логируем тестовое событие
                    val logEvent = loggingService.logActivity(
                        userId = userId,
                        eventType = "TEST_LOG",
                        deviceModel = call.request.headers["User-Agent"],
                        details = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            "test" to true
                        )
                    )
                    
                    // Проверяем, что событие было создано
                    call.respond(HttpStatusCode.OK, mapOf(
                        "status" to "success",
                        "message" to "Log event created successfully",
                        "eventId" to logEvent.timestamp.toString(),
                        "userId" to logEvent.userId,
                        "eventType" to logEvent.eventType
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "status" to "error",
                        "message" to "Failed to create log event: ${e.message}"
                    ))
                }
            }
        }
    }
}