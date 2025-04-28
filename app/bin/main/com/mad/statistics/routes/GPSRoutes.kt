package com.mad.statistics.routes

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
                    val gpsData = gpsService.getGPSDataByExerciseId(exerciseId)
                    call.respond(mapOf("gps_data" to gpsData))
                } catch (e: Exception) {
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
                    gpsService.saveGPSData(gpsData)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respondText(
                        "Error uploading GPS data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
}
