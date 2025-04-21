package app.herocraft.di

import app.herocraft.core.security.UserService
import app.herocraft.features.images.ImageProcessor
import app.herocraft.features.images.ImageService
import app.herocraft.features.images.S3Processor
import app.herocraft.features.notifications.NotificationManager
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import io.ktor.server.application.*
import io.ktor.server.engine.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val serviceModule = module {

    single {
        val application: Application = get()
        NotificationManager(application.environment.config)
    }

    singleOf(::UserService)

    singleOf(::ImageProcessor)

    single {
        val application: Application = get()

        val config = application.environment.config

        val s3CredentialsProvider = StaticCredentialsProvider() {
            accountId = config.property("herocraft.s3.id").getString()
            accessKeyId = config.property("herocraft.s3.key").getString()
            secretAccessKey = config.property("herocraft.s3.secret").getString()
        }

        val baseUrl = config.property("herocraft.s3.baseUrl").getString()
        val endpointUrl = config.property("herocraft.s3.endpoint").getString()

        S3Processor(s3CredentialsProvider, baseUrl, endpointUrl)
    }

    singleOf(::ImageService)

}