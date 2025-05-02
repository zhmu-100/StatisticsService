import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.1.0"
  kotlin("plugin.serialization") version "1.9.0"
  id("com.ncorti.ktfmt.gradle") version "0.22.0"
  application
  jacoco
}

group = "com.mad"

version = "0.0.1"

application { mainClass.set("com.mad.statistics.ApplicationKt") }

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

val ktorVersion = "2.3.3"
val koinVersion = "3.4.3"
val logbackVersion = "1.4.11"
val clickhouseVersion = "0.4.6"
val kotlinxDatetimeVersion = "0.4.0"

dependencies {
  implementation("io.ktor:ktor-server-core:$ktorVersion")
  implementation("io.ktor:ktor-server-netty:$ktorVersion")
  implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
  implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
  implementation("io.ktor:ktor-server-cors:$ktorVersion")

  // Logger from feat/add-logger branch
  implementation("com.github.poplopok:Logger:1.0.6")

  implementation("io.lettuce:lettuce-core:6.2.4.RELEASE")

  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")

  implementation(fileTree("libs") { include("*.jar") })

  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-client-json:$ktorVersion")

  implementation("io.insert-koin:koin-core:$koinVersion")
  implementation("io.insert-koin:koin-ktor:$koinVersion")
  implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

  implementation("ch.qos.logback:logback-classic:$logbackVersion")

  implementation("com.clickhouse:clickhouse-jdbc:$clickhouseVersion")
  implementation("com.clickhouse:clickhouse-http-client:$clickhouseVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")

  testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
  testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
  testImplementation("io.mockk:mockk:1.13.5")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
}

tasks.withType<KotlinCompile> { kotlinOptions { jvmTarget = "17" } }

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

jacoco { toolVersion = "0.8.10" }

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
}

tasks.jacocoTestCoverageVerification {
  dependsOn(tasks.jacocoTestReport)
  violationRules {
    rule {
      element = "BUNDLE"
      limit {
        counter = "INSTRUCTION"
        value = "COVEREDRATIO"
        minimum = "0.50".toBigDecimal()
      }
    }
  }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }

tasks.build { dependsOn(tasks.check) }

kotlin { jvmToolchain(17) }
