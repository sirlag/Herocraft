package app.herocraft.features.images

import app.herocraft.core.models.CardFace
import app.herocraft.core.models.IvionCardImage
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.uuid.Uuid


class S3Processor(
    private val s3CredentialsProvider: CredentialsProvider,
    private val s3BaseUrl: String,
    private val s3EndpointUrl: String
) {

    private val logger = KotlinLogging.logger {}

    suspend fun storeInBucket(
        bytes: ByteArray,
        face: CardFace,
        size: ImageProcessor.ImageSize,
        format: ImageProcessor.ImageFormat,
        id: Uuid
    ): IvionCardImage {

        val pathKey = "/$face/$size/${id.toHexDashString()}$format"
        val myContentType = when (format) {
            ImageProcessor.ImageFormat.PNG -> "image/png"
            ImageProcessor.ImageFormat.JPEG -> "image/jpeg"
        }
        val outputStream = ByteStream.fromBytes(bytes)
        logger.trace { "Entering prototype s3 method" }

        val request =
            PutObjectRequest {
                bucket = "cards"
                key = pathKey
                body = outputStream
                contentType = myContentType
                tagging = "public=yes"
            }

        S3Client {
            region = "auto"
            credentialsProvider = s3CredentialsProvider
            forcePathStyle = true
            endpointUrl = Url.parse(s3EndpointUrl)
        }.use { s3 ->
            val response = s3.putObject(request)
        }
        logger.trace { "Prototype s3 successful" }

        return IvionCardImage(
            cardId = id,
            face = face,
            variant = size,
            uri = "$s3BaseUrl$pathKey",
            mimeType = myContentType,
            byteSize = bytes.size,
        )

    }
}
