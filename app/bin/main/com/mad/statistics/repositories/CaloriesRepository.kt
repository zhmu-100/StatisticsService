package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.CaloriesData
import com.mad.statistics.models.common.UserMetadata
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import java.util.UUID

class CaloriesRepository(clickHouseServiceClient: ClickHouseServiceClient) : RepositoryBase(clickHouseServiceClient) {
    
    fun saveCaloriesData(caloriesData: CaloriesData) {
        runBlocking {
            val dataToInsert = buildJsonObject {
                put("id", caloriesData.meta.id.ifEmpty { UUID.randomUUID().toString() })
                put("user_id", caloriesData.meta.userId)
                put("timestamp", caloriesData.meta.timestamp.toString())
                put("calories", caloriesData.calories)
            }
            
            clickHouseServiceClient.insert("calories_data", listOf(dataToInsert))
        }
    }
    
    fun getCaloriesDataByUserId(userId: String): List<CaloriesData> {
        return runBlocking {
            val columns = listOf("id", "user_id", "timestamp", "calories")
            val filters = mapOf("user_id" to userId)
            val orderBy = "timestamp ASC"
            
            val result = clickHouseServiceClient.select("calories_data", columns, filters, orderBy)
            
            result.map { row ->
                val id = row.jsonObject["id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                val userIdFromDb = row.jsonObject["user_id"]?.jsonPrimitive?.content ?: ""
                val timestamp = Instant.parse(row.jsonObject["timestamp"]?.jsonPrimitive?.content ?: "")
                val calories = row.jsonObject["calories"]?.jsonPrimitive?.double ?: 0.0
                
                CaloriesData(
                    meta = UserMetadata(
                        id = id,
                        userId = userIdFromDb,
                        timestamp = timestamp
                    ),
                    calories = calories
                )
            }
        }
    }
}