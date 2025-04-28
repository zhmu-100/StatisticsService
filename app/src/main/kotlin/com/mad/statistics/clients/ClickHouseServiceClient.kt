package com.mad.statistics.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import com.mad.statistics.config.AppConfig

class ClickHouseServiceClient(
    private val baseUrl: String = "http://localhost:8080"
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun checkConnection(): Boolean {
        return try {
            val endpoint = if (AppConfig.dbMode.equals("gateway", ignoreCase = true)) {
                "$baseUrl/ping"
            } else {
                "$baseUrl/ping"
            }
            
            val response = client.get(endpoint) {
                contentType(ContentType.Application.Json)
            }
            
            logger.info("Connection check response: ${response.status}")
            response.status.isSuccess()
        } catch (e: Exception) {
            logger.error("Error checking connection to ClickHouse Service: ${e.message}", e)
            false
        }
    }

    suspend fun insert(table: String, data: List<JsonObject>) {
        try {
            val response = client.post("$baseUrl/insert") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("table", table)
                        put("data", JsonArray(data))
                    }.toString()
                )
            }
    
            if (response.status.isSuccess()) {
                logger.info("Insert response: ${response.status}")
            } else {
                val responseText = response.bodyAsText()
                logger.error("Insert error: ${response.status}, body: $responseText")
            }
        } catch (e: Exception) {
            logger.error("Error inserting data: ${e.message}", e)
            throw e
        }
    }
    

    suspend fun select(table: String, columns: List<String>, filters: Map<String, Any>, orderBy: String? = null): JsonArray {
        try {
            val filtersJson = buildJsonObject {
                filters.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, value)
                        is Number -> put(key, value)
                        is Boolean -> put(key, value)
                        else -> put(key, value.toString())
                    }
                }
            }
    
            val requestBody = buildJsonObject {
                put("table", table)
                put("columns", JsonArray(columns.map { JsonPrimitive(it) }))
                put("filters", filtersJson)
                if (orderBy != null) {
                    put("orderBy", orderBy)
                }
            }
    
            logger.info("Sending select request to $baseUrl/select with body: $requestBody")
    
            val response = client.post("$baseUrl/select") {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }
    
            val responseText = response.bodyAsText()
            logger.info("Select response: ${response.status}, body: $responseText")
    
            // Парсим JSON-ответ
            val jsonElement = Json.parseToJsonElement(responseText)
            
            // Проверяем формат ответа и извлекаем результат
            if (jsonElement is JsonObject) {
                // Если ответ в формате {"status": "success", "result": [...]}
                if (jsonElement.containsKey("result")) {
                    val resultElement = jsonElement["result"]
                    if (resultElement is JsonArray) {
                        return resultElement
                    }
                }
                // Если ответ в формате {"status": "success", "data": [...]}
                if (jsonElement.containsKey("data")) {
                    val dataElement = jsonElement["data"]
                    if (dataElement is JsonArray) {
                        return dataElement
                    }
                }
                // Если ответ в формате {"rows": [...]}
                if (jsonElement.containsKey("rows")) {
                    val rowsElement = jsonElement["rows"]
                    if (rowsElement is JsonArray) {
                        return rowsElement
                    }
                }
            }
            
            // Если не удалось извлечь массив данных, возвращаем пустой массив
            return JsonArray(emptyList())
        } catch (e: Exception) {
            logger.error("Error selecting data: ${e.message}", e)
            throw e
        }
    }
}