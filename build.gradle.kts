import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val kotlin_version: String by project
val logback_version: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.flyway)
    id("com.strumenta.antlr-kotlin") version "1.0.5"
    id("com.github.ben-manes.versions") version "0.52.0"
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
version = System.getenv("RELEASE_TAG") ?: "0.0.1"

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
    fatJar {
        archiveFileName.set(System.getenv("RELEASE_TAG")?.let { "herocraft-$it.jar" } ?: "herocrafter.jar")
    }
}

flyway {
    url = "jdbc:postgresql://localhost:5432/herocrafter"
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
    implementation(libs.kotlinx.datetime)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    implementation(libs.bundles.exposed)
    implementation("com.h2database:h2:2.2.224")
    implementation(libs.postgres)
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation(libs.bundles.flyway)

    implementation("eu.vendeli:rethis:0.2.9")
    implementation("io.lettuce:lettuce-core:6.6.0.RELEASE")

    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("app.softwork:kotlinx-uuid-core:0.1.5")

    implementation("org.apache.commons:commons-email:1.6.0")

    implementation(kotlincrypto.random.crypto.rand)

    implementation("com.strumenta:antlr-kotlin-runtime:1.0.3")

    implementation(awssdk.services.s3)

    implementation(libs.bundles.arrow)

    implementation(libs.config4k)

    implementation (project.dependencies.platform(libs.koin.bom))
    implementation(libs.bundles.koin)

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