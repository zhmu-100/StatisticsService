package com.mad.statistics.routes

import com.mad.client.LoggerClient
import com.mad.statistics.models.GPSData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.models.common.GPSPosition
import com.mad.statistics.services.CaloriesService
import com.mad.statistics.services.GPSService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.just
import io.mockk.Runs
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GPSRoutesTest {
    private val json = Json { encodeDefaults = true }
    private val mockLogger = mockk<LoggerClient>(relaxed = true)
    private val mockService = mockk<GPSService>()

    private fun testModules(
        service: GPSService = mockService,
        logger: LoggerClient = mockLogger
    ) = listOf(
        module { single<GPSService> { service } },
        module { single<LoggerClient>    { logger  } }
    )
    private val testMeta = ExerciseMetadata(
        id = "meta-1",
        exerciseId = "ex1",
        timestamp = Instant.parse("2025-04-28T12:00:00Z")
    )
    private val testPosition = GPSPosition(
        timestamp = Instant.parse("2025-04-28T12:30:00Z"),
        latitude = 10.0,
        longitude = 20.0,
        altitude = 100.0,
        speed = 5.5,
        accuracy = 3.0
    )
    private val testData = GPSData(
        meta = testMeta,
        positions = listOf(testPosition)
    )

    @Test
    fun `GET gps without exercise_id returns BadRequest`() {
        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<GPSService> { mockk() } }) }
            configureGPSRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/gps")
            assertEquals(HttpStatusCode.BadRequest, call.response.status())
            assertEquals("Missing exercise_id parameter", call.response.content)
        }
    }

    @Test
    fun `GET gps with valid id returns data`() {
        //val mockService = mockk<GPSService>()
        every { mockService.getGPSDataByExerciseId("ex1") } returns listOf(testData)

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureGPSRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/gps?exercise_id=ex1")
            assertEquals(HttpStatusCode.OK, call.response.status())
            val content = call.response.content!!
            assertTrue(content.contains("\"latitude\":10.0"))
            assertTrue(content.contains("\"longitude\":20.0"))
            assertTrue(content.contains("gps_data"))
            verify(exactly = 1) { mockService.getGPSDataByExerciseId("ex1") }
        }
    }

    @Test
    fun `GET gps when service throws returns InternalServerError`() {
        //al mockService = mockk<GPSService>()
        every { mockService.getGPSDataByExerciseId(any()) } throws RuntimeException("fail")

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureGPSRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/gps?exercise_id=ex1")
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
            assertTrue(call.response.content!!.contains("Error retrieving GPS data: fail"))
        }
    }

    @Test
    fun `POST gps with valid body returns OK`() {
        //val mockService = mockk<GPSService>()
        every { mockService.saveGPSData(testData) } just Runs

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureGPSRoutes()
        }) {
            val call = handleRequest(HttpMethod.Post, "/api/statistics/gps") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(testData))
            }
            assertEquals(HttpStatusCode.OK, call.response.status())
            verify(exactly = 1) { mockService.saveGPSData(testData) }
        }
    }

    @Test
    fun `POST gps when service throws returns InternalServerError`() {
        //val mockService = mockk<GPSService>()
        every { mockService.saveGPSData(testData) } throws IllegalStateException("bad")

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureGPSRoutes()
        }) {
            val call = handleRequest(HttpMethod.Post, "/api/statistics/gps") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(testData))
            }
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
            assertTrue(call.response.content!!.contains("Error uploading GPS data: bad"))
        }
    }
}
