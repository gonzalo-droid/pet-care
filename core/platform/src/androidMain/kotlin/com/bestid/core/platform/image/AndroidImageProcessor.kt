package com.bestid.core.platform.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.bestid.core.domain.port.ImageProcessor
import com.bestid.core.domain.port.ProcessedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * `ImageProcessor` con Bitmap + ExifInterface (sección 8): endereza según la orientación EXIF,
 * reduce a [MAX_PHOTO_SIDE] de lado largo y vuelve a comprimir en JPEG [JPEG_QUALITY].
 * `Bitmap.compress` no escribe EXIF, así que la foto sale sin metadatos (ni ubicación).
 */
class AndroidImageProcessor : ImageProcessor {
    override suspend fun process(original: ByteArray): ProcessedImage? = withContext(Dispatchers.Default) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(original, 0, original.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        // Decodifica ya reducida a la potencia de 2 que deja el lado largo en MAX_PHOTO_SIDE o más.
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_PHOTO_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeByteArray(original, 0, original.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return@withContext null

        val matrix = orientationMatrix(readOrientation(original))
        val scale = MAX_PHOTO_SIDE.toFloat() / max(decoded.width, decoded.height)
        if (scale < 1f) matrix.postScale(scale, scale)
        val result = if (matrix.isIdentity) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        val jpeg = ByteArrayOutputStream().use { out ->
            result.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
        val processed = ProcessedImage(jpeg, result.width, result.height)
        if (result !== decoded) result.recycle()
        decoded.recycle()
        processed
    }

    private fun readOrientation(bytes: ByteArray): Int =
        runCatching { ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
            .getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun orientationMatrix(orientation: Int) = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { postRotate(270f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }
    }
}

