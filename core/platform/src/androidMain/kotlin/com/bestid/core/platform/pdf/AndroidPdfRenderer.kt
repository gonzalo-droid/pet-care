package com.bestid.core.platform.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ceil
import kotlin.math.max

/** `PdfRenderer` con `PdfDocument` (sección 8). El diseño lo pone [PdfLayout]. */
class AndroidPdfRenderer : PdfRenderer {
    override suspend fun render(spec: PdfDocumentSpec, target: String) = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        try {
            val canvas = AndroidPdfCanvas(document)
            PdfLayout(canvas, spec).render()
            canvas.finish()
            File(target).apply { parentFile?.mkdirs() }.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
        }
    }
}

private class AndroidPdfCanvas(private val document: PdfDocument) : PdfCanvas {
    private var number = 0
    private var page: PdfDocument.Page = start()
    private val canvas: Canvas get() = page.canvas
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun start(): PdfDocument.Page {
        number++
        val info = PdfDocument.PageInfo.Builder(PdfLayout.PAGE_WIDTH.toInt(), PdfLayout.PAGE_HEIGHT.toInt(), number).create()
        return document.startPage(info)
    }

    override fun newPage() {
        document.finishPage(page)
        page = start()
    }

    fun finish() = document.finishPage(page)

    private fun paint(style: PdfText, color: Int = 0) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = style.size.toFloat()
        typeface = Typeface.create(Typeface.SANS_SERIF, if (style.bold) Typeface.BOLD else Typeface.NORMAL)
        this.color = color
    }

    private fun layout(text: String, style: PdfText, width: Double, color: Int = 0) =
        StaticLayout.Builder.obtain(text, 0, text.length, paint(style, color), max(1, width.toInt()))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.1f)
            .build()

    override fun measure(text: String, style: PdfText, width: Double): Double = layout(text, style, width).height.toDouble()

    override fun drawText(text: String, style: PdfText, color: Int, x: Double, y: Double, width: Double) {
        canvas.save()
        canvas.translate(x.toFloat(), y.toFloat())
        layout(text, style, width, color).draw(canvas)
        canvas.restore()
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double, color: Int) {
        fill.color = color
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat(), fill)
    }

    override fun imageSize(path: String): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        return if (options.outWidth > 0 && options.outHeight > 0) options.outWidth to options.outHeight else null
    }

    override fun drawImage(path: String, x: Double, y: Double, width: Double, height: Double) {
        val (w, h) = imageSize(path) ?: return
        // A 150 ppp basta para leer la cartilla y el PDF no pesa de más.
        val targetPx = ceil(max(width, height) * 150 / 72).toInt()
        var sample = 1
        while (max(w, h) / (sample * 2) >= targetPx) sample *= 2
        val bitmap: Bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return
        canvas.drawBitmap(bitmap, null, RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
        bitmap.recycle()
    }
}
