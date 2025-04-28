package com.mad.statistics.routes

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
                    val caloriesData = caloriesService.getCaloriesDataByUserId(userId)
                    call.respond(mapOf("calories_data" to caloriesData))
                } catch (e: Exception) {
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
                    caloriesService.saveCaloriesData(caloriesData)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respondText(
                        "Error uploading calories data: ${e.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
}
