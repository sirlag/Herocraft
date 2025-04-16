package app.herocraft.features.images

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

class ImageProcessor {

    private val logger = KotlinLogging.logger {}

    enum class ImageSize(val width: Int, val height: Int) {
        FULL(750, 1050),
        LARGE(672, 942),
        NORMAL(488, 684),
        SMALL(146, 204);

        override fun toString(): String {
            return when(this) {
                FULL -> "full"
                LARGE -> "large"
                NORMAL -> "normal"
                SMALL -> "small"
            }
        }
    }

    enum class ImageFormat {
        JPEG,
        PNG;

        override fun toString(): String {
            return when(this) {
                JPEG -> ".jpg"
                PNG -> ".png"
            }
        }
    }

    suspend fun resizeImage(inputStream: InputStream, targetSize: ImageSize): ByteArray {
        logger.trace { "Starting to process image" }
        val input = withContext(Dispatchers.IO) {
            ImageIO.read(inputStream)
        }

        val output = BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB)

        val graphics = output.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.drawImage(input, 0, 0, targetSize.width, targetSize.height, null)
        graphics.dispose()

        logger.trace { "Imaged resized. Starting save." }

        val outStream = ByteArrayOutputStream()

        logger.trace { "out pipe created" }

        withContext(Dispatchers.IO) {
            ImageIO.createImageOutputStream(outStream)
        }.use {
            val writer = ImageIO.getImageWritersByFormatName("jpg").next()

            val param = writer.defaultWriteParam
            if (param.canWriteCompressed()) {
                param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                param.compressionQuality = 0.9f
            }

            writer.output = it

            logger.trace { "configured writer" }

            writer.write(null, IIOImage(output, null, null), param)
            writer.dispose()
        }

        logger.trace { "Stream created, returning" }

        return outStream.toByteArray()
    }
}