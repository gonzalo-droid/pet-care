package com.bestid.core.platform.pdf

/**
 * Contenido del PDF, ya con los textos en español (los arma la pantalla desde Compose Resources).
 * Cada plataforma solo dibuja: el orden, la paginación y las tablas están en [PdfLayout].
 */
data class PdfDocumentSpec(
    val blocks: List<PdfBlock>,
    /** Pie de cada página (sección 12). */
    val footer: String,
    val colors: PdfColors,
)

/** Colores ARGB tomados de los tokens del design system (regla 1). */
data class PdfColors(
    val primary: Int,
    val onPrimary: Int,
    val ink: Int,
    val muted: Int,
    val line: Int,
    val soft: Int,
)

sealed interface PdfBlock {
    /** Franja de la marca, foto (ruta absoluta) y nombre con sus datos principales. */
    data class Header(val brand: String, val title: String, val subtitle: String?, val photoPath: String?) : PdfBlock
    data class Section(val title: String) : PdfBlock
    /** Pares etiqueta y valor en dos columnas. */
    data class Fields(val rows: List<Pair<String, String>>) : PdfBlock
    /** Tabla con encabezado que se repite al pasar de página. [weights]: ancho relativo de cada columna. */
    data class Table(val headers: List<String>, val rows: List<List<String>>, val weights: List<Float>) : PdfBlock
    data class Paragraph(val text: String) : PdfBlock
    /** Fotos a página completa, una por página, con su título. */
    data class Images(val items: List<Pair<String, String>>) : PdfBlock
}

enum class PdfText(val size: Double, val bold: Boolean) {
    TITLE(20.0, true),
    HEADING(13.0, true),
    BODY(10.0, false),
    BODY_BOLD(10.0, true),
    SMALL(8.0, false),
}

/** Lo único que implementa cada plataforma. Unidades: puntos, origen arriba a la izquierda. */
interface PdfCanvas {
    /** Alto que ocupa [text] envuelto en [width]. */
    fun measure(text: String, style: PdfText, width: Double): Double
    fun drawText(text: String, style: PdfText, color: Int, x: Double, y: Double, width: Double)
    fun fillRect(x: Double, y: Double, width: Double, height: Double, color: Int)
    /** Tamaño en píxeles de la imagen, o `null` si no se puede leer. */
    fun imageSize(path: String): Pair<Int, Int>?
    fun drawImage(path: String, x: Double, y: Double, width: Double, height: Double)
    fun newPage()
}

/** Genera el PDF en [target] (ruta absoluta), en A4 vertical, sin internet. */
interface PdfRenderer {
    suspend fun render(spec: PdfDocumentSpec, target: String)
}
