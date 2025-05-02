package com.mad.statistics.routes

import com.mad.client.LoggerClient
import com.mad.statistics.models.GPSData
import com.mad.statistics.services.GPSService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureGPSRoutes() {
    val gpsService: GPSService by inject()
    
    val logger by inject<LoggerClient>()
    
    routing {
        route("/api/statistics") {
            // Получение GPS данных по ID упражнения
            get("/gps") {
                val exerciseId = call.request.queryParameters["exercise_id"]
                    ?: return@get call.respondText(
                        "Missing exercise_id parameter",
                        status = HttpStatusCode.BadRequest
                    )
                
                try {
                    logger.logActivity(
                        event = "GPS data requested",
                        userId = call.request.headers["User-Id"],
                        additionalData = mapOf("exercise_id" to exerciseId)
                    )
                    
                    val gpsData = gpsService.getGPSDataByExerciseId(exerciseId)
                    call.respond(mapOf("gps_data" to gpsData))
                } catch (e: Exception) {
                    logger.logError(
                        event = "GPS data retrieval failed",
                        errorMessage = e.message ?: "Unknown error",
                        userId = call.request.headers["User-Id"],
                        stackTrace = e.stackTraceToString()
                    )
                    
                    call.respondText(
                        "Error retrieving GPS data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            
            // Загрузка GPS данных
            post("/gps") {
                try {
                    val gpsData = call.receive<GPSData>()
                    
                    // Создаем карту с информацией о данных GPS
                    val logData = mutableMapOf<String, String>()
                    
                    // Безопасно добавляем данные из структуры GPSData
                    try {
                        // Добавляем данные из meta
                        gpsData.meta?.let { meta ->
                            logData["exercise_id"] = meta.exerciseId
                            logData["timestamp"] = meta.timestamp.toString()
                            logData["id"] = meta.id
                        }
                        
                        // Добавляем информацию о позициях
                        gpsData.positions?.let { positions ->
                            logData["positions_count"] = positions.size.toString()
                            
                            // Добавляем информацию о первой и последней позиции, если они есть
                            if (positions.isNotEmpty()) {
                                val first = positions.first()
                                val last = positions.last()
                                
                                logData["first_position_time"] = first.timestamp.toString()
                                logData["last_position_time"] = last.timestamp.toString()
                            }
                        }
                    } catch (e: Exception) {
                        // Если не удалось получить данные, логируем это
                        logData["data_available"] = "false"
                    }
                    
                    logger.logActivity(
                        event = "GPS data upload",
                        userId = call.request.headers["User-Id"],
                        additionalData = logData
                    )
                    
                    gpsService.saveGPSData(gpsData)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    logger.logError(
                        event = "GPS data upload failed",
                        errorMessage = e.message ?: "Unknown error",
                        userId = call.request.headers["User-Id"],
                        stackTrace = e.stackTraceToString()
                    )
                    
                    call.respondText(
                        "Error uploading GPS data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
    
}