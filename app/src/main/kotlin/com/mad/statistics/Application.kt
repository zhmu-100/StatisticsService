package com.mad.statistics

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
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("com.mad.statistics.Application")
    logger.info("Starting Statistics Service with configuration:")
    logger.info("Port: ${AppConfig.port}")
    logger.info("Database Mode: ${AppConfig.dbMode}")
    logger.info("Database Host: ${AppConfig.dbHost}")
    logger.info("Database Port: ${AppConfig.dbPort}")
    logger.info("ClickHouse Service URL: ${AppConfig.clickhouseServiceUrl}")

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
            call.respondText(text = "500: ${cause.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
        }
    }
    
    configureGPSRoutes()
    configureHeartRateRoutes()
    configureCaloriesRoutes()
}
