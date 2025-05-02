package com.mad.statistics.routes

import com.mad.client.LoggerClient
import com.mad.statistics.models.HeartRateData
import com.mad.statistics.services.HeartRateService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureHeartRateRoutes() {
    val heartRateService: HeartRateService by inject()

    // Используем внедрение зависимостей для LoggerClient
    val logger by inject<LoggerClient>()

    routing {
        route("/api/statistics") {
            // Получение данных о сердечном ритме по ID упражнения
            get("/heartrate") {
                val exerciseId = call.request.queryParameters["exercise_id"]
                    ?: return@get call.respondText(
                        "Missing exercise_id parameter",
                        status = HttpStatusCode.BadRequest
                    )

                try {
                    // Логируем запрос данных о сердечном ритме
                    logger.logActivity(
                        event = "Heart rate data requested",
                        userId = call.request.headers["User-Id"],
                        additionalData = mapOf("exercise_id" to exerciseId)
                    )

                    val heartRateData = heartRateService.getHeartRateDataByExerciseId(exerciseId)

                    // Логируем успешное получение данных
                    logger.logActivity(
                        event = "Heart rate data retrieved",
                        userId = call.request.headers["User-Id"],
                        additionalData = mapOf(
                            "exercise_id" to exerciseId,
                            "records_count" to heartRateData.size.toString()
                        )
                    )

                    call.respond(mapOf("heart_rate_data" to heartRateData))
                } catch (e: Exception) {
                    // Логируем ошибку при получении данных
                    logger.logError(
                        event = "Heart rate data retrieval failed",
                        errorMessage = e.message ?: "Unknown error",
                        userId = call.request.headers["User-Id"],
                        stackTrace = e.stackTraceToString()
                    )

                    call.respondText(
                        "Error retrieving heart rate data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }

            // Загрузка данных о сердечном ритме
            post("/heartrate") {
                try {
                    val heartRateData = call.receive<HeartRateData>()

                    val logData = mutableMapOf<String, String>()

                    // Безопасно добавляем данные
                    try {
                        // Добавляем данные из meta
                        heartRateData.meta?.let { meta ->
                            logData["exercise_id"] = meta.exerciseId
                            logData["timestamp"] = meta.timestamp.toString()
                            logData["id"] = meta.id
                        }

                        // Добавляем информацию о BPM
                        logData["bpm"] = heartRateData.bpm.toString()
                    } catch (e: Exception) {
                        logData["data_available"] = "false"
                    }

                    // Логируем загрузку данных о сердечном ритме
                    logger.logActivity(
                        event = "Heart rate data upload",
                        userId = call.request.headers["User-Id"],
                        additionalData = logData
                    )

                    heartRateService.saveHeartRateData(heartRateData)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    // Логируем ошибку при загрузке данных
                    logger.logError(
                        event = "Heart rate data upload failed",
                        errorMessage = e.message ?: "Unknown error",
                        userId = call.request.headers["User-Id"],
                        stackTrace = e.stackTraceToString()
                    )

                    call.respondText(
                        "Error uploading heart rate data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
}