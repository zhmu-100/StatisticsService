package com.mad.statistics.clients

import com.mad.client.LoggerClient
import com.mad.model.LogLevel
import com.mad.statistics.config.AppConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class ClickHouseServiceClient(private val baseUrl: String = "http://localhost:8080") {
  private val loggerClient = LoggerClient()
  private val client =
      HttpClient(CIO) {
        install(ContentNegotiation) {
          json(
              Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
              })
        }
      }

  suspend fun checkConnection(): Boolean {
    return try {
      val endpoint =
          if (AppConfig.dbMode.equals("gateway", ignoreCase = true)) {
            "$baseUrl/ping"
          } else {
            "$baseUrl/ping"
          }

      val response = client.get(endpoint) { contentType(ContentType.Application.Json) }

      loggerClient.logActivity(
          event = "Connection check response: ${response.status}", level = LogLevel.INFO)
      response.status.isSuccess()
    } catch (e: Exception) {
      loggerClient.logError(
          event = "Error checking connection to ClickHouse Service",
          errorMessage = e.message ?: "Unknown error",
          stackTrace = e.stackTraceToString())
      false
    }
  }

  suspend fun insert(table: String, data: List<JsonObject>) {
    try {
      val response =
          client.post("$baseUrl/insert") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                      put("table", table)
                      put("data", JsonArray(data))
                    }
                    .toString())
          }

      if (response.status.isSuccess()) {
        loggerClient.logActivity(
            event = "Insert response: ${response.status}", level = LogLevel.INFO)
      } else {
        val responseText = response.bodyAsText()
        loggerClient.logError(
            event = "Insert error: ${response.status}", errorMessage = "body: $responseText")
      }
    } catch (e: Exception) {
      loggerClient.logError(
          event = "Error inserting data",
          errorMessage = e.message ?: "Unknown error",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  suspend fun select(
      table: String,
      columns: List<String>,
      filters: Map<String, Any>,
      orderBy: String? = null
  ): JsonArray {
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

      loggerClient.logActivity(
          event = "Sending select request to $baseUrl/select with body: $requestBody",
          level = LogLevel.INFO)

      val response =
          client.post("$baseUrl/select") {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
          }

      val responseText = response.bodyAsText()
      loggerClient.logActivity(
          event = "Select response: ${response.status}, body: $responseText", level = LogLevel.INFO)

      val jsonElement = Json.parseToJsonElement(responseText)

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
      loggerClient.logError(
          event = "Error selecting data",
          errorMessage = e.message ?: "Unknown error",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
