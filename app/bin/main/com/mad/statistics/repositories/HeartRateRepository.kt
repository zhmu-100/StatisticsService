package com.mad.statistics.repositories

import com.mad.statistics.models.HeartRateData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.utils.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.sql.Timestamp
import java.util.UUID

class HeartRateRepository : RepositoryBase() {
    
    fun saveHeartRateData(heartRateData: HeartRateData) {
        getConnection().use { connection ->
            val sql = """
                INSERT INTO heart_rate_data (id, exercise_id, timestamp, bpm)
                VALUES (?, ?, ?, ?)
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, heartRateData.meta.id.ifEmpty { UUID.randomUUID().toString() })
                statement.setString(2, heartRateData.meta.exerciseId)
                statement.setTimestamp(3, Timestamp.from(heartRateData.meta.timestamp.toJavaInstant()))
                statement.setInt(4, heartRateData.bpm)
                
                statement.executeUpdate()
            }
        }
    }
    
    fun getHeartRateDataByExerciseId(exerciseId: String): List<HeartRateData> {
        val result = mutableListOf<HeartRateData>()
        
        getConnection().use { connection ->
            val sql = """
                SELECT id, exercise_id, timestamp, bpm
                FROM heart_rate_data
                WHERE exercise_id = ?
                ORDER BY timestamp
            """.trimIndent()
            
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, exerciseId)
                
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val id = resultSet.getString("id")
                        val exerciseIdFromDb = resultSet.getString("exercise_id")
                        val timestamp = resultSet.getTimestamp("timestamp").toInstant().toKotlinInstant()
                        val bpm = resultSet.getInt("bpm")
                        
                        val metadata = ExerciseMetadata(
                            id = id,
                            exerciseId = exerciseIdFromDb,
                            timestamp = timestamp
                        )
                        
                        result.add(
                            HeartRateData(
                                meta = metadata,
                                bpm = bpm
                            )
                        )
                    }
                }
            }
        }
        
        return result
    }
}