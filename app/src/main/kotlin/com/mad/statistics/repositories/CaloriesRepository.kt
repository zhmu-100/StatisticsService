package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.CaloriesData
import com.mad.statistics.models.common.UserMetadata
import com.mad.statistics.utils.toClickHouseDateTime
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import java.util.UUID
import com.mad.statistics.utils.parseClickHouseDateTime

class CaloriesRepository(clickHouseServiceClient: ClickHouseServiceClient) : RepositoryBase(clickHouseServiceClient) {

    fun saveCaloriesData(caloriesData: CaloriesData) {
        runBlocking {
            val dataToInsert = buildJsonObject {
                put("id", caloriesData.meta.id.ifEmpty { UUID.randomUUID().toString() })
                put("user_id", caloriesData.meta.userId)
                put("timestamp", caloriesData.meta.timestamp.toClickHouseDateTime())
                put("calories", caloriesData.calories)
            }
            
            clickHouseServiceClient.insert("calories_data", listOf(dataToInsert))
        }
    }

    fun getCaloriesDataByUserId(userId: String): List<CaloriesData> {
        return try {
            val columns = listOf("id", "user_id", "timestamp", "calories")
            val filters = mapOf("user_id" to userId)
            val orderBy = "timestamp ASC"
            
            val result = runBlocking {
                clickHouseServiceClient.select("calories_data", columns, filters, orderBy)
            }
            
            // Преобразуем результат в список CaloriesData
            result.mapNotNull { element ->
                try {
                    if (element !is JsonObject) {
                        logWarn("Expected JsonObject but got ${element::class.simpleName}")
                        return@mapNotNull null
                    }
                    
                    val id = element["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString()
                    val userIdFromDb = element["user_id"]?.jsonPrimitive?.contentOrNull ?: ""
                    val timestampStr = element["timestamp"]?.jsonPrimitive?.contentOrNull ?: ""
                    val calories = element["calories"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    
                    val timestamp = try {
                        parseClickHouseDateTime(timestampStr)
                    } catch (e: Exception) {
                        logError("Error parsing timestamp: $timestampStr", e)
                        Instant.fromEpochMilliseconds(0)
                    }
                    
                    CaloriesData(
                        meta = UserMetadata(
                            id = id,
                            userId = userIdFromDb,
                            timestamp = timestamp
                        ),
                        calories = calories
                    )
                } catch (e: Exception) {
                    logError("Error mapping calories data: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            logError("Error retrieving calories data: ${e.message}", e)
            emptyList()
        }
    }
    
}
