package com.mad.statistics.repositories

import com.mad.statistics.utils.toClickHouseDateTime

import com.mad.statistics.clients.ClickHouseServiceClient
import com.mad.statistics.models.GPSData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.just
import io.mockk.Runs
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GPSRepositoryTest {
    private lateinit var client: ClickHouseServiceClient
    private lateinit var repository: GPSRepository

    private val meta = ExerciseMetadata(
        id = "meta-1",
        exerciseId = "ex1",
        timestamp = Instant.parse("2025-04-28T12:00:00Z")
    )
    private val pos1 = GPSPosition(
        timestamp = Instant.parse("2025-04-28T12:30:00Z"),
        latitude = 10.0,
        longitude = 20.0,
        altitude = 100.0,
        speed = 5.5,
        accuracy = 3.0
    )
    private val pos2 = GPSPosition(
        timestamp = Instant.parse("2025-04-28T12:31:00Z"),
        latitude = 11.0,
        longitude = 21.0,
        altitude = 110.0,
        speed = 6.5,
        accuracy = 4.0
    )

    @BeforeTest
    fun setup() {
        client = mockk(relaxed = true)
        repository = GPSRepository(client)
    }

    @Test
    fun `saveGPSData should call insert with one JSON object per position`() {
        val data = GPSData(meta = meta, positions = listOf(pos1, pos2))
        val slotList = slot<List<JsonObject>>()
        coEvery { client.insert("gps_data", capture(slotList)) } just Runs

        repository.saveGPSData(data)

        coVerify(exactly = 1) { client.insert("gps_data", any()) }
        val inserted = slotList.captured
        assertEquals(2, inserted.size)
        // verify first element fields
        val obj1 = inserted[0]
        assertTrue(obj1["id"]?.jsonPrimitive?.content!!.isNotBlank())
        assertEquals("ex1", obj1["exercise_id"]?.jsonPrimitive?.content)
        assertEquals(meta.timestamp.toClickHouseDateTime(), obj1["timestamp"]?.jsonPrimitive?.content)
        assertEquals(pos1.timestamp.toClickHouseDateTime(), obj1["position_timestamp"]?.jsonPrimitive?.content)
        assertEquals(10.0, obj1["latitude"]?.jsonPrimitive?.double)
        assertEquals(20.0, obj1["longitude"]?.jsonPrimitive?.double)
        assertEquals(100.0, obj1["altitude"]?.jsonPrimitive?.double)
        assertEquals(5.5, obj1["speed"]?.jsonPrimitive?.double)
        assertEquals(3.0, obj1["accuracy"]?.jsonPrimitive?.double)
    }

    @Test
    fun `getGPSDataByExerciseId should group positions into GPSData`() {
        // build two JSON rows for same exercise
        val j1 = buildJsonObject {
            put("id", JsonPrimitive("id1"))
            put("exercise_id", JsonPrimitive("ex1"))
            put("timestamp", JsonPrimitive("2025-04-28T12:00:00Z"))
            put("position_timestamp", JsonPrimitive("2025-04-28T12:30:00Z"))
            put("latitude", JsonPrimitive(10.0))
            put("longitude", JsonPrimitive(20.0))
            put("altitude", JsonPrimitive(100.0))
            put("speed", JsonPrimitive(5.5))
            put("accuracy", JsonPrimitive(3.0))
        }
        val j2 = buildJsonObject {
            put("id", JsonPrimitive("id2"))
            put("exercise_id", JsonPrimitive("ex1"))
            put("timestamp", JsonPrimitive("2025-04-28T12:00:00Z"))
            put("position_timestamp", JsonPrimitive("2025-04-28T12:31:00Z"))
            put("latitude", JsonPrimitive(11.0))
            put("longitude", JsonPrimitive(21.0))
            put("altitude", JsonPrimitive(110.0))
            put("speed", JsonPrimitive(6.5))
            put("accuracy", JsonPrimitive(4.0))
        }
        val arr = JsonArray(listOf<JsonElement>(j1, j2))
        coEvery { client.select(any(), any(), any(), any()) } returns arr

        val result = repository.getGPSDataByExerciseId("ex1")
        assertEquals(1, result.size)
        val gd = result.first()
        // metadata from first
        assertEquals("id1", gd.meta.id)
        assertEquals("ex1", gd.meta.exerciseId)
        assertEquals(Instant.parse("2025-04-28T12:00:00Z"), gd.meta.timestamp)
        // positions list
        assertEquals(2, gd.positions.size)
        val p1 = gd.positions[0]
        assertEquals(Instant.parse("2025-04-28T12:30:00Z"), p1.timestamp)
        assertEquals(10.0, p1.latitude)
        val p2 = gd.positions[1]
        assertEquals(Instant.parse("2025-04-28T12:31:00Z"), p2.timestamp)
        assertEquals(11.0, p2.latitude)
    }

    @Test
    fun `getGPSDataByExerciseId skips non-JsonObject elements`() {
        val arr = JsonArray(listOf<JsonElement>(JsonPrimitive("bad"), JsonPrimitive("worse")))
        coEvery { client.select(any(), any(), any(), any()) } returns arr

        val result = repository.getGPSDataByExerciseId("ex1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getGPSDataByExerciseId returns empty list when select throws`() {
        coEvery { client.select(any(), any(), any(), any()) } throws RuntimeException("fail")
        val result = repository.getGPSDataByExerciseId("ex1")
        assertTrue(result.isEmpty())
    }
}
