package com.mad.statistics.config

import io.ktor.server.config.*

object AppConfig {
    private val config = ApplicationConfig("application.conf")
    

    val port: Int = System.getenv("PORT")?.toIntOrNull() 
        ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() 
        ?: 8082
    
    val dbMode: String = System.getenv("DB_MODE") 
        ?: config.propertyOrNull("ktor.database.mode")?.getString() 
        ?: "local"
    
    val dbHost: String = System.getenv("DB_HOST") 
        ?: config.propertyOrNull("ktor.database.host")?.getString() 
        ?: "localhost"
        
    val dbPort: String = System.getenv("DB_PORT") 
        ?: config.propertyOrNull("ktor.database.port")?.getString() 
        ?: "8080"
    
    val clickhouseServiceUrl: String = 
        if (dbMode.equals("gateway", ignoreCase = true)) 
            "http://$dbHost:$dbPort/api/clickhouse"
        else 
            "http://$dbHost:$dbPort"
    
    val fullClickhouseServiceUrl: String get() = clickhouseServiceUrl
}


