package com.mad.statistics.config

object AppConfig {
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8082
    val clickhouseServiceUrl: String = System.getenv("CLICKHOUSE_SERVICE_URL") ?: "http://localhost:8080"
    val databaseUser: String = System.getenv("DATABASE_USER") ?: "default"
    val databasePassword: String = System.getenv("DATABASE_PASSWORD") ?: ""
    val databaseUrl: String = System.getenv("DATABASE_URL") ?: "jdbc:clickhouse://localhost:9000"
}