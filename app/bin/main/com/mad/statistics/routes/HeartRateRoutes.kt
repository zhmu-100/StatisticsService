package com.mad.statistics.routes

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
                    val heartRateData = heartRateService.getHeartRateDataByExerciseId(exerciseId)
                    call.respond(mapOf("heart_rate_data" to heartRateData))
                } catch (e: Exception) {
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
                    heartRateService.saveHeartRateData(heartRateData)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respondText(
                        "Error uploading heart rate data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
}
