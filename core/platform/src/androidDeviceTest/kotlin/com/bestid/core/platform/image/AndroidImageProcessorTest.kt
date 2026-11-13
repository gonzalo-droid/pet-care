package com.bestid.core.platform.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Corre en el emulador del CI (job "Android (avisos en emulador)"). */
@RunWith(AndroidJUnit4::class)
class AndroidImageProcessorTest {
    private val processor = AndroidImageProcessor()

    /** JPEG de [width]x[height]: mitad izquierda roja y derecha azul, con EXIF de orientación y ubicación. */
    private fun jpeg(width: Int, height: Int, orientation: Int = ExifInterface.ORIENTATION_NORMAL): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) for (y in 0 until height) bitmap.setPixel(x, y, if (x < width / 2) Color.RED else Color.BLUE)
        val dir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File.createTempFile("prueba", ".jpg", dir)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            setLatLong(-12.0464, -77.0428) // Lima
            saveAttributes()
        }
        return file.readBytes().also { file.delete() }
    }

    @Test
    fun rotatesUsingExifOrientation() = runTest {
        val result = assertNotNull(processor.process(jpeg(300, 200, ExifInterface.ORIENTATION_ROTATE_90)))
        assertEquals(200, result.width)
        assertEquals(300, result.height)
        val bitmap = BitmapFactory.decodeByteArray(result.jpeg, 0, result.jpeg.size)
        assertEquals(200, bitmap.width)
        // Girada 90° a la derecha: el rojo (antes a la izquierda) queda arriba.
        assertEquals(Color.RED, nearest(bitmap.getPixel(100, 20)))
        assertEquals(Color.BLUE, nearest(bitmap.getPixel(100, 280)))
    }

    @Test
    fun scalesDownTheLongSideTo2000() = runTest {
        val result = assertNotNull(processor.process(jpeg(4000, 1000)))
        assertEquals(2000, result.width)
        assertEquals(500, result.height)
    }

    @Test
    fun keepsSmallPhotosAtTheirSize() = runTest {
        val result = assertNotNull(processor.process(jpeg(640, 480)))
        assertEquals(640, result.width)
        assertEquals(480, result.height)
    }

    @Test
    fun removesMetadataIncludingLocation() = runTest {
        val original = jpeg(300, 200)
        assertNotNull(ExifInterface(ByteArrayInputStream(original)).latLong)
        val result = assertNotNull(processor.process(original))
        val exif = ExifInterface(ByteArrayInputStream(result.jpeg))
        assertNull(exif.latLong)
        assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED))
    }

    @Test
    fun rejectsBytesThatAreNotAnImage() = runTest {
        assertNull(processor.process(byteArrayOf(1, 2, 3)))
    }

    private fun nearest(color: Int): Int = if (Color.red(color) > Color.blue(color)) Color.RED else Color.BLUE
}
