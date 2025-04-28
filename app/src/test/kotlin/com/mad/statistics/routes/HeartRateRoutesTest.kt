package com.mad.statistics.routes

import com.mad.statistics.models.HeartRateData
import com.mad.statistics.models.common.ExerciseMetadata
import com.mad.statistics.services.HeartRateService
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeartRateRoutesTest {
    private val json = Json { encodeDefaults = true }
    private val testMeta = ExerciseMetadata(
        id = "meta-1",
        exerciseId = "ex1",
        timestamp = Instant.parse("2025-04-28T12:00:00Z")
    )

    @Test
    fun `GET heartrate without exercise_id returns BadRequest`() {
        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<HeartRateService> { mockk() } }) }
            configureHeartRateRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/heartrate")
            assertEquals(HttpStatusCode.BadRequest, call.response.status())
            assertEquals("Missing exercise_id parameter", call.response.content)
        }
    }

    @Test
    fun `GET heartrate with valid id returns data`() {
        val mockService = mockk<HeartRateService>()
        val data = HeartRateData(meta = testMeta, bpm = 75)
        every { mockService.getHeartRateDataByExerciseId("ex1") } returns listOf(data)

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<HeartRateService> { mockService } }) }
            configureHeartRateRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/heartrate?exercise_id=ex1")
            assertEquals(HttpStatusCode.OK, call.response.status())
            val content = call.response.content!!
            // Should contain the serialized bpm and exercise_id
            assertTrue(content.contains("\"bpm\":75"))
            assertTrue(content.contains("\"exerciseId\":\"ex1\""))
            assertTrue(content.contains("heart_rate_data"))
            verify(exactly = 1) { mockService.getHeartRateDataByExerciseId("ex1") }
        }
    }

    @Test
    fun `GET heartrate when service throws returns InternalServerError`() {
        val mockService = mockk<HeartRateService>()
        every { mockService.getHeartRateDataByExerciseId(any()) } throws RuntimeException("fail")

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<HeartRateService> { mockService } }) }
            configureHeartRateRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/heartrate?exercise_id=ex1")
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
            assertTrue(call.response.content!!.contains("Error retrieving heart rate data: fail"))
        }
    }

    @Test
    fun `POST heartrate with valid body returns OK`() {
        val mockService = mockk<HeartRateService>()
        val data = HeartRateData(meta = testMeta, bpm = 60)
        every { mockService.saveHeartRateData(data) } just Runs

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<HeartRateService> { mockService } }) }
            configureHeartRateRoutes()
        }) {
            val call = handleRequest(HttpMethod.Post, "/api/statistics/heartrate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(data))
            }
            assertEquals(HttpStatusCode.OK, call.response.status())
            verify(exactly = 1) { mockService.saveHeartRateData(data) }
        }
    }

    @Test
    fun `POST heartrate when service throws returns InternalServerError`() {
        val mockService = mockk<HeartRateService>()
        val data = HeartRateData(meta = testMeta, bpm = 100)
        every { mockService.saveHeartRateData(data) } throws IllegalStateException("bad")

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<HeartRateService> { mockService } }) }
            configureHeartRateRoutes()
        }) {
            val call = handleRequest(HttpMethod.Post, "/api/statistics/heartrate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(data))
            }
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
            assertTrue(call.response.content!!.contains("Error uploading heart rate data: bad"))
        }
    }
}
