package com.mad.statistics.repositories

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.HeartRateData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.utils.toClickHouseDateTime
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.just
import io.mockk.Runs
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeartRateRepositoryTest {
    private lateinit var client: ClickHouseServiceClient
    private lateinit var repository: HeartRateRepository

    private val testMeta = ExerciseMetadata(
        id = "id1",
        exerciseId = "ex1",
        timestamp = Instant.parse("2025-04-28T12:00:00Z")
    )

    @BeforeTest
    fun setup() {
        client = mockk()
        repository = HeartRateRepository(client)
    }

    @Test
    fun `saveHeartRateData should call insert with correct JSON`() {
        val data = HeartRateData(meta = testMeta, bpm = 72)
        val slotList = slot<List<JsonObject>>()
        coEvery { client.insert("heart_rate_data", capture(slotList)) } just Runs

        repository.saveHeartRateData(data)

        coVerify(exactly = 1) { client.insert("heart_rate_data", any()) }
        val inserted = slotList.captured
        assertEquals(1, inserted.size)
        val obj = inserted[0]
        assertEquals("id1", obj["id"]?.jsonPrimitive?.content)
        assertEquals("ex1", obj["exercise_id"]?.jsonPrimitive?.content)
        assertEquals(testMeta.timestamp.toClickHouseDateTime(), obj["timestamp"]?.jsonPrimitive?.content)
        assertEquals(72, obj["bpm"]?.jsonPrimitive?.int)
    }

    @Test
    fun `getHeartRateDataByExerciseId should map JSON to HeartRateData list`() {
        val jsonObj = buildJsonObject {
            put("id", JsonPrimitive("id1"))
            put("exercise_id", JsonPrimitive("ex1"))
            put("timestamp", JsonPrimitive("2025-04-28T12:00:00Z"))
            put("bpm", JsonPrimitive(80))
        }
        val jsonArray = JsonArray(listOf(jsonObj))
        coEvery { client.select(any(), any(), any(), any()) } returns jsonArray

        val result = repository.getHeartRateDataByExerciseId("ex1")

        assertEquals(1, result.size)
        val hr = result.first()
        assertEquals("id1", hr.meta.id)
        assertEquals("ex1", hr.meta.exerciseId)
        assertEquals(Instant.parse("2025-04-28T12:00:00Z"), hr.meta.timestamp)
        assertEquals(80, hr.bpm)
    }

    @Test
    fun `getHeartRateDataByExerciseId returns empty list when select throws`() {
        coEvery { client.select(any(), any(), any(), any()) } throws RuntimeException("fail")

        val result = repository.getHeartRateDataByExerciseId("ex1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getHeartRateDataByExerciseId skips non-JsonObject elements`() {
        val invalidElement = JsonPrimitive("bad")
        val validObj = buildJsonObject {
            put("id", JsonPrimitive("id2"))
            put("exercise_id", JsonPrimitive("ex1"))
            put("timestamp", JsonPrimitive("2025-04-28T12:00:00Z"))
            put("bpm", JsonPrimitive(90))
        }
        val jsonArray = JsonArray(listOf(invalidElement, validObj))
        coEvery { client.select(any(), any(), any(), any()) } returns jsonArray

        val result = repository.getHeartRateDataByExerciseId("ex1")
        assertEquals(1, result.size)
        assertEquals("id2", result.first().meta.id)
    }

    @Test
    fun `getHeartRateDataByExerciseId handles invalid timestamp`() {
        val badTimestampObj = buildJsonObject {
            put("id", JsonPrimitive("id3"))
            put("exercise_id", JsonPrimitive("ex1"))
            put("timestamp", JsonPrimitive("not-a-timestamp"))
            put("bpm", JsonPrimitive(65))
        }
        val jsonArray = JsonArray(listOf(badTimestampObj))
        coEvery { client.select(any(), any(), any(), any()) } returns jsonArray

        val result = repository.getHeartRateDataByExerciseId("ex1")
        assertEquals(1, result.size)
        val hr = result.first()
        assertEquals(Instant.fromEpochMilliseconds(0), hr.meta.timestamp)
    }
}
