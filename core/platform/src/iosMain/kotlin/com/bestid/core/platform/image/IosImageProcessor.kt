package com.bestid.core.platform.image

import com.bestid.core.domain.port.ImageProcessor
import com.bestid.core.domain.port.ProcessedImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.posix.memcpy
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * `ImageProcessor` con UIImage (sección 8): `UIImage` entiende JPEG y HEIC y aplica la orientación
 * al dibujar; se dibuja de nuevo reducida a [MAX_PHOTO_SIDE] y se exporta en JPEG [JPEG_QUALITY].
 * El JPEG nuevo no lleva los metadatos del original (ni ubicación).
 */
@OptIn(ExperimentalForeignApi::class)
class IosImageProcessor : ImageProcessor {
    override suspend fun process(original: ByteArray): ProcessedImage? = withContext(Dispatchers.IO) {
        if (original.isEmpty()) return@withContext null
        val image = UIImage.imageWithData(original.toNSData()) ?: return@withContext null
        val (w, h) = image.size.useContents { width * image.scale to height * image.scale }
        if (w <= 0.0 || h <= 0.0) return@withContext null
        val factor = min(1.0, MAX_PHOTO_SIDE / max(w, h))
        val width = (w * factor).roundToInt().coerceAtLeast(1)
        val height = (h * factor).roundToInt().coerceAtLeast(1)
        val format = UIGraphicsImageRendererFormat().apply {
            scale = 1.0
            opaque = true
        }
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width.toDouble(), height.toDouble()), format = format)
        val jpeg = renderer.JPEGDataWithCompressionQuality(JPEG_QUALITY / 100.0) { _ ->
            image.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
        }
        ProcessedImage(jpeg.toByteArray(), width, height)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}
