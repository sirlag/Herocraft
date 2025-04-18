import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val flyway_version: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    id("org.flywaydb.flyway") version "11.7.0"
    id("com.strumenta.antlr-kotlin") version "1.0.2"
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlin.ExperimentalStdlibApi")
        optIn.add("io.lettuce.core.ExperimentalLettuceCoroutinesApi")
    }
    sourceSets {
        main {
            kotlin {
                srcDir(layout.buildDirectory.dir("generatedAntlr"))
            }
        }
    }
}

group = "app.herocraft"
version = "0.0.1"

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.7.0")
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.withType<ShadowJar> {
    mergeServiceFiles("META-INF/services/org.flywaydb.core.extensibility.Plugin")
}

ktor {
    fatJar
}

flyway {
    url = "jdbc:postgresql://tower:5432/herocrafter"
    user = "postgres"
    password = "password"
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-compression-jvm")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.5")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    implementation(libs.ktor.client.cio)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.0-RC.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    implementation(libs.bundles.exposed)
    implementation("com.h2database:h2:2.2.224")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-database-postgresql:$flyway_version")
    implementation("eu.vendeli:rethis:0.2.9")
    implementation("io.lettuce:lettuce-core:6.5.5.RELEASE")

    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("app.softwork:kotlinx-uuid-core:0.1.5")

    implementation("org.apache.commons:commons-email:1.6.0")

    implementation(kotlincrypto.random.crypto.rand)

    implementation("com.strumenta:antlr-kotlin-runtime:1.0.2")

    implementation(awssdk.services.s3)

    implementation(libs.bundles.arrow)

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

val generateKotlinGrammerSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammerSource") {
    dependsOn("cleanGenerateKotlinGrammerSource")

    source = fileTree(layout.projectDirectory.dir("antlr")) {
        include("**/*.g4")
    }

    val pkgName = "app.herocraft.antlr.generated"
    packageName = pkgName

    arguments = listOf("-visitor")

    val outDir = "generatedAntlr/${pkgName.replace(".", "/")}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
}

tasks.withType<KotlinCompilationTask<*>> {
    dependsOn(generateKotlinGrammerSource)
}