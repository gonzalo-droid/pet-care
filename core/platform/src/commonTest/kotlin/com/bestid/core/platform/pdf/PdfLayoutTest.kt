package com.bestid.core.platform.pdf

import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PdfLayoutTest {
    /** Lienzo falso: cada carácter mide medio tamaño de letra de ancho; registra lo dibujado por página. */
    private class FakeCanvas(private val images: Map<String, Pair<Int, Int>> = emptyMap()) : PdfCanvas {
        val pages = mutableListOf(mutableListOf<String>())
        override fun measure(text: String, style: PdfText, width: Double): Double {
            val perLine = (width / (style.size * 0.5)).toInt().coerceAtLeast(1)
            return ceil(text.length.toDouble() / perLine).coerceAtLeast(1.0) * style.size * 1.3
        }
        override fun drawText(text: String, style: PdfText, color: Int, x: Double, y: Double, width: Double) {
            check(y >= 0 && y < PdfLayout.PAGE_HEIGHT) { "Texto fuera de la página: $text en y=$y" }
            pages.last() += text
        }
        override fun fillRect(x: Double, y: Double, width: Double, height: Double, color: Int) = Unit
        override fun imageSize(path: String) = images[path]
        override fun drawImage(path: String, x: Double, y: Double, width: Double, height: Double) {
            check(x >= PdfLayout.MARGIN - 0.01 && x + width <= PdfLayout.PAGE_WIDTH - PdfLayout.MARGIN + 0.01) { "Imagen fuera del ancho" }
            pages.last() += "img:$path"
        }
        override fun newPage() {
            pages += mutableListOf<String>()
        }
    }

    private val colors = PdfColors(1, 2, 3, 4, 5, 6)
    private fun spec(vararg blocks: PdfBlock) = PdfDocumentSpec(blocks.toList(), footer = "Generado por BestID", colors = colors)

    @Test
    fun everyPageHasTheFooterAndALongTableRepeatsItsHeader() {
        val canvas = FakeCanvas()
        val rows = List(80) { listOf("21 dic 2025", "Rabia, calicivirus", "Lote $it") }
        val pages = PdfLayout(canvas, spec(PdfBlock.Section("Vacunas"), PdfBlock.Table(listOf("Fecha", "Tipos", "Lote"), rows, listOf(1f, 2f, 1f)))).render()
        assertTrue(pages > 1)
        assertEquals(pages, canvas.pages.size)
        canvas.pages.forEach { page ->
            assertTrue("Generado por BestID" in page)
            assertTrue("Fecha" in page) // encabezado repetido
        }
        assertEquals(80, canvas.pages.sumOf { page -> page.count { it.startsWith("Lote ") } })
    }

    @Test
    fun headerSkipsAPhotoThatCannotBeRead() {
        val canvas = FakeCanvas()
        PdfLayout(canvas, spec(PdfBlock.Header("BestID", "Lilith", "Gata · Mestiza", photoPath = "/no-existe.jpg"))).render()
        assertTrue("Lilith" in canvas.pages.single())
        assertFalse(canvas.pages.single().any { it.startsWith("img:") })
    }

    @Test
    fun eachCardPhotoGoesOnItsOwnPage() {
        val canvas = FakeCanvas(mapOf("/p1.jpg" to (1500 to 2000), "/p2.jpg" to (2000 to 1500)))
        val pages = PdfLayout(canvas, spec(PdfBlock.Paragraph("Hola"), PdfBlock.Images(listOf("Página 1" to "/p1.jpg", "Página 2" to "/p2.jpg", "Rota" to "/x.jpg")))).render()
        assertEquals(3, pages)
        assertEquals(listOf("Página 1", "img:/p1.jpg"), canvas.pages[1].filterNot { it.startsWith("Generado") })
        assertEquals(listOf("Página 2", "img:/p2.jpg"), canvas.pages[2].filterNot { it.startsWith("Generado") })
    }
}
