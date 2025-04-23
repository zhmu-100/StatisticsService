import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

group = "com.mad"
version = "0.0.1"

application {
    mainClass.set("com.mad.statistics.ApplicationKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.3"
val koinVersion = "3.4.3"
val logbackVersion = "1.4.11"
val clickhouseVersion = "0.4.6"
val kotlinxDatetimeVersion = "0.4.0"

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    
    // Koin
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    
    // ClickHouse
    // implementation("com.clickhouse:clickhouse-jdbc:$clickhouseVersion")
    // implementation("com.clickhouse:clickhouse-http-client:$clickhouseVersion")
    
    implementation("org.postgresql:postgresql:42.6.0")

    // Kotlinx DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform()
}
