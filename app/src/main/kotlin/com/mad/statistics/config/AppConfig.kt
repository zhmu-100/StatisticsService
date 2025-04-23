package com.mad.statistics.config

object AppConfig {
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8082
    val clickhouseUrl: String = System.getenv("CLICKHOUSE_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
    val clickhouseUser: String = System.getenv("CLICKHOUSE_USER") ?: "postgres"
    val clickhousePassword: String = System.getenv("CLICKHOUSE_PASSWORD") ?: "postgres"
}
