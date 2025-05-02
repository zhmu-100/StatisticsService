package com.mad.statistics.routes

import com.mad.client.LoggerClient
import com.mad.statistics.models.CaloriesData
import com.mad.statistics.models.common.UserMetadata
import com.mad.statistics.services.CaloriesService
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

class CaloriesRoutesTest {
    private val json = Json { encodeDefaults = true }
    private val mockLogger = mockk<LoggerClient>(relaxed = true)
    private val mockService = mockk<CaloriesService>()

    private fun testModules(
        service: CaloriesService = mockService,
        logger: LoggerClient  = mockLogger
    ) = listOf(
        module { single<CaloriesService> { service } },
        module { single<LoggerClient>    { logger  } }
    )
    private val testMeta = UserMetadata(
        id = "meta-1",
        userId = "user-1",
        timestamp = Instant.parse("2025-04-28T12:00:00Z")
    )
    private val testData = CaloriesData(
        meta = testMeta,
        calories = 250.0
    )

    @Test
    fun `GET calories without user_id returns BadRequest`() {
        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(module { single<CaloriesService> { mockk() } }) }
            configureCaloriesRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/calories")
            assertEquals(HttpStatusCode.BadRequest, call.response.status())
            assertEquals("Missing user_id parameter", call.response.content)
        }
    }

    @Test
    fun `GET calories with valid id returns data`() {
        //val mockService = mockk<CaloriesService>()
        every { mockService.getCaloriesDataByUserId("user-1") } returns listOf(testData)

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureCaloriesRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/calories?user_id=user-1")
            assertEquals(HttpStatusCode.OK, call.response.status())
            val content = call.response.content!!
            assertTrue(content.contains("\"calories\":250.0"))
            assertTrue(content.contains("\"userId\":\"user-1\""))
            assertTrue(content.contains("calories_data"))
            verify(exactly = 1) { mockService.getCaloriesDataByUserId("user-1") }
        }
    }

    @Test
    fun `GET calories when service throws returns InternalServerError`() {
        //val mockService = mockk<CaloriesService>()
        every { mockService.getCaloriesDataByUserId(any()) } throws RuntimeException("fail")

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureCaloriesRoutes()
        }) {
            val call = handleRequest(HttpMethod.Get, "/api/statistics/calories?user_id=user-1")
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
            assertTrue(call.response.content!!.contains("Error retrieving calories data: fail"))
        }
    }

    @Test
    fun `POST calories with valid body returns OK`() {
        //val mockService = mockk<CaloriesService>()
        every { mockService.saveCaloriesData(testData) } just Runs

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureCaloriesRoutes()
        }) {
            val call = handleRequest(HttpMethod.Post, "/api/statistics/calories") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(testData))
            }
            assertEquals(HttpStatusCode.OK, call.response.status())
            verify(exactly = 1) { mockService.saveCaloriesData(testData) }
        }
    }

    @Test
    fun `POST calories when service throws returns InternalServerError`() {
        //val mockService = mockk<CaloriesService>()
        every { mockService.saveCaloriesData(testData) } throws IllegalStateException("bad")

        withTestApplication({
            install(ContentNegotiation) { json() }
            install(Koin) { modules(testModules()) }
            configureCaloriesRoutes()
        }) {
            val call = handleRequest(HttpMethod.Post, "/api/statistics/calories") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(testData))
            }
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
            assertTrue(call.response.content!!.contains("Error uploading calories data: bad"))
        }
    }
}
