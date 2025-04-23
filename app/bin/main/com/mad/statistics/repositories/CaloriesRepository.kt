package com.mad.statistics.repositories

import com.mad.statistics.models.CaloriesData
import com.mad.statistics.models.common.UserMetadata
import com.mad.statistics.utils.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.sql.Timestamp
import java.util.UUID

class CaloriesRepository : RepositoryBase() {
    
    fun saveCaloriesData(caloriesData: CaloriesData) {
        getConnection().use { connection ->
            val sql = """
                INSERT INTO calories_data (id, user_id, timestamp, calories)
                VALUES (?, ?, ?, ?)
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, caloriesData.meta.id.ifEmpty { UUID.randomUUID().toString() })
                statement.setString(2, caloriesData.meta.userId)
                statement.setTimestamp(3, Timestamp.from(caloriesData.meta.timestamp.toJavaInstant()))
                statement.setDouble(4, caloriesData.calories)
                
                statement.executeUpdate()
            }
        }
    }
    
    fun getCaloriesDataByUserId(userId: String): List<CaloriesData> {
        val result = mutableListOf<CaloriesData>()
        
        getConnection().use { connection ->
            val sql = """
                SELECT id, user_id, timestamp, calories
                FROM calories_data
                WHERE user_id = ?
                ORDER BY timestamp
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, userId)
                
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val id = resultSet.getString("id")
                        val userIdFromDb = resultSet.getString("user_id")
                        val timestamp = resultSet.getTimestamp("timestamp").toInstant().toKotlinInstant()
                        val calories = resultSet.getDouble("calories")
                        
                        val metadata = UserMetadata(
                            id = id,
                            userId = userIdFromDb,
                            timestamp = timestamp
                        )
                        
                        result.add(
                            CaloriesData(
                                meta = metadata,
                                calories = calories
                            )
                        )
                    }
                }
            }
        }
        
        return result
    }
}