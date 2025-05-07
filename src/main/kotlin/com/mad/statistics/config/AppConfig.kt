package com.mad.statistics.config

object AppConfig {
  val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8082

  val dbMode: String = System.getenv("DB_MODE") ?: "local"

  val dbHost: String = System.getenv("DB_HOST") ?: "clickhouse-service"

  val dbPort: String = System.getenv("DB_PORT") ?: "8091"

  val clickhouseServiceUrl: String =
      if (dbMode.equals("gateway", ignoreCase = true)) "http://$dbHost:$dbPort/api/clickhouse"
      else "http://$dbHost:$dbPort"

  val fullClickhouseServiceUrl: String
    get() = clickhouseServiceUrl

  val redisHost: String = System.getenv("REDIS_HOST") ?: "redis"
  val redisPort: Int = System.getenv("REDIS_PORT")?.toIntOrNull() ?: 6379
  val redisPassword: String? = System.getenv("REDIS_PASSWORD") ?: ""
}
