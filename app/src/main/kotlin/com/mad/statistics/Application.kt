package com.mad.statistics

import com.mad.client.LoggerClient
import com.mad.model.LogLevel
import com.mad.statistics.config.AppConfig
import com.mad.statistics.config.DatabaseConfig
import com.mad.statistics.config.configureKoin
import com.mad.statistics.routes.configureCaloriesRoutes
import com.mad.statistics.routes.configureGPSRoutes
import com.mad.statistics.routes.configureHeartRateRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main() {
    val logger = LoggerClient()
    logger.logActivity(
        event = "Starting Statistics Service with configuration:",
        level = LogLevel.INFO
    )
    logger.logActivity(
        event = "Port: ${AppConfig.port}",
        level = LogLevel.INFO
    )
    logger.logActivity(
        event = "Database Mode: ${AppConfig.dbMode}",
        level = LogLevel.INFO
    )
    logger.logActivity(
        event = "Database Host: ${AppConfig.dbHost}",
        level = LogLevel.INFO
    )
    logger.logActivity(
        event = "Database Port: ${AppConfig.dbPort}",
        level = LogLevel.INFO
    )
    logger.logActivity(
        event = "ClickHouse Service URL: ${AppConfig.clickhouseServiceUrl}",
        level = LogLevel.INFO
    )

    embeddedServer(Netty, port = AppConfig.port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseConfig.init()

    configureKoin()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val logger = LoggerClient()
            logger.logError(
                event = "Internal Server Error",
                errorMessage = cause.message ?: "Unknown error",
                stackTrace = cause.stackTraceToString()
            )
            call.respondText(text = "500: ${cause.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
        }
    }

    configureGPSRoutes()
    configureHeartRateRoutes()
    configureCaloriesRoutes()
}