package app.herocraft.features.images

import app.herocraft.core.models.CardFace
import app.herocraft.core.models.IvionCard
import app.herocraft.core.models.IvionCardImage
import app.herocraft.features.images.ImageProcessor.ImageFormat
import app.herocraft.features.images.ImageProcessor.ImageSize
import app.herocraft.features.search.CardImageRepo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.time.measureTimedValue
import kotlin.uuid.Uuid

class ImageService(
    private val imageProcessor: ImageProcessor,
    private val s3Processor: S3Processor,
    private val cardImageRepo: CardImageRepo,
) {

    private val httpClient: HttpClient by lazy {
        HttpClient(CIO) {}
    }

    private val logger = KotlinLogging.logger {}

    private fun wixIvionUrl(imageUUID: Uuid) =
        "https://static.wixstatic.com/media/b096d7_${imageUUID.toHexString()}~mv2.png"

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun processAll(cards: List<IvionCard>) {

        val (imagesToStore, timeTaken) = measureTimedValue {
            cards.asFlow()
                .flowOn(Dispatchers.IO)
                .flatMapMerge { card ->
                    flow {
                        val frontImages = processFace(CardFace.FRONT, card)
                        emitAll(frontImages.asFlow())
                        if (card.secondUUID != null) {
                            val backImages = processFace(CardFace.BACK, card)
                            emitAll(backImages.asFlow())
                        }
                    }
                }.toList()
        }

        logger.debug { "Processed ${imagesToStore.size} images in $timeTaken ms" }

        cardImageRepo.addAllCardImages(imagesToStore)

    }

    private suspend fun processFace(face: CardFace, card: IvionCard): List<IvionCardImage> {

        val sourceId = if (CardFace.FRONT == face) card.ivionUUID else card.secondUUID ?: return emptyList()

        val response: HttpResponse = httpClient.get(wixIvionUrl(sourceId))
        if (response.status != HttpStatusCode.OK) {
            logger.error { "Unable to fetch face: $face, card: ${card.name}" }
            return emptyList()
        }
        val sourceImage = response.readRawBytes()
        val largeImage = imageProcessor.resizeImage(sourceImage.inputStream(), targetSize = ImageSize.LARGE)
        val normalImage = imageProcessor.resizeImage(sourceImage.inputStream(), targetSize = ImageSize.NORMAL)
        val smallImage = imageProcessor.resizeImage(sourceImage.inputStream(), targetSize = ImageSize.SMALL)

        val cardFull = s3Processor.storeInBucket(sourceImage, face, ImageSize.FULL, ImageFormat.PNG, card.id)
        val cardLarge = s3Processor.storeInBucket(largeImage, face, ImageSize.LARGE, ImageFormat.JPEG, card.id)
        val cardNormal = s3Processor.storeInBucket(normalImage, face, ImageSize.NORMAL, ImageFormat.JPEG, card.id)
        val cardSmall = s3Processor.storeInBucket(smallImage, face, ImageSize.SMALL, ImageFormat.JPEG, card.id)

        return listOfNotNull(cardFull, cardLarge, cardNormal, cardSmall)

    }
}