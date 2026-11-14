package com.bestid.core.platform.pdf

import kotlin.math.max
import kotlin.math.min

/** Diseño común del PDF: A4 vertical (595 × 842 pt), márgenes, saltos de página, tablas y pie. */
class PdfLayout(private val canvas: PdfCanvas, private val spec: PdfDocumentSpec) {
    private val colors = spec.colors
    private var y = MARGIN
    private var pages = 0

    /** Dibuja todo y devuelve cuántas páginas usó. */
    fun render(): Int {
        startPage()
        spec.blocks.forEach { block ->
            when (block) {
                is PdfBlock.Header -> header(block)
                is PdfBlock.Section -> section(block)
                is PdfBlock.Fields -> fields(block)
                is PdfBlock.Table -> table(block)
                is PdfBlock.Paragraph -> paragraph(block)
                is PdfBlock.Images -> images(block)
            }
        }
        return pages
    }

    private fun startPage() {
        if (pages > 0) canvas.newPage()
        pages++
        y = MARGIN
        val footerHeight = canvas.measure(spec.footer, PdfText.SMALL, CONTENT_WIDTH)
        canvas.drawText(spec.footer, PdfText.SMALL, colors.muted, MARGIN, PAGE_HEIGHT - MARGIN + GAP - footerHeight, CONTENT_WIDTH)
    }

    private val bottom get() = PAGE_HEIGHT - MARGIN - FOOTER_SPACE

    private fun ensure(height: Double) {
        if (y + height > bottom) startPage()
    }

    private fun header(block: PdfBlock.Header) {
        val band = canvas.measure(block.brand, PdfText.HEADING, CONTENT_WIDTH) + GAP * 2
        canvas.fillRect(MARGIN, y, CONTENT_WIDTH, band, colors.primary)
        canvas.drawText(block.brand, PdfText.HEADING, colors.onPrimary, MARGIN + GAP * 2, y + GAP, CONTENT_WIDTH - GAP * 4)
        y += band + GAP * 2

        val photo = block.photoPath?.takeIf { canvas.imageSize(it) != null }
        val textX = if (photo != null) MARGIN + PHOTO + GAP * 2 else MARGIN
        val textWidth = CONTENT_WIDTH - (textX - MARGIN)
        val titleHeight = canvas.measure(block.title, PdfText.TITLE, textWidth)
        val subtitleHeight = block.subtitle?.let { canvas.measure(it, PdfText.BODY, textWidth) } ?: 0.0
        photo?.let { canvas.drawImage(it, MARGIN, y, PHOTO, PHOTO) }
        canvas.drawText(block.title, PdfText.TITLE, colors.ink, textX, y, textWidth)
        block.subtitle?.let { canvas.drawText(it, PdfText.BODY, colors.muted, textX, y + titleHeight + GAP / 2, textWidth) }
        y += max(if (photo != null) PHOTO else 0.0, titleHeight + GAP / 2 + subtitleHeight) + GAP * 2
    }

    private fun section(block: PdfBlock.Section) {
        val height = canvas.measure(block.title, PdfText.HEADING, CONTENT_WIDTH)
        // Un título nunca queda solo al pie de la página.
        ensure(height + GAP + ROW_MIN * 2)
        y += GAP
        canvas.drawText(block.title, PdfText.HEADING, colors.ink, MARGIN, y, CONTENT_WIDTH)
        y += height + GAP / 2
        canvas.fillRect(MARGIN, y, CONTENT_WIDTH, LINE, colors.line)
        y += GAP
    }

    private fun fields(block: PdfBlock.Fields) {
        val labelWidth = CONTENT_WIDTH * 0.32
        val valueWidth = CONTENT_WIDTH - labelWidth - GAP
        block.rows.forEach { (label, value) ->
            val height = max(canvas.measure(label, PdfText.BODY, labelWidth), canvas.measure(value, PdfText.BODY, valueWidth))
            ensure(height)
            canvas.drawText(label, PdfText.BODY, colors.muted, MARGIN, y, labelWidth)
            canvas.drawText(value, PdfText.BODY, colors.ink, MARGIN + labelWidth + GAP, y, valueWidth)
            y += height + GAP / 2
        }
    }

    private fun table(block: PdfBlock.Table) {
        val total = block.weights.sum()
        val widths = block.weights.map { CONTENT_WIDTH * it / total }
        fun rowHeight(cells: List<String>, style: PdfText) =
            cells.indices.maxOf { i -> canvas.measure(cells[i], style, widths[i] - CELL * 2) } + CELL * 2

        fun drawRow(cells: List<String>, style: PdfText, background: Int?) {
            val height = rowHeight(cells, style)
            background?.let { canvas.fillRect(MARGIN, y, CONTENT_WIDTH, height, it) }
            var x = MARGIN
            cells.forEachIndexed { i, text ->
                canvas.drawText(text, style, colors.ink, x + CELL, y + CELL, widths[i] - CELL * 2)
                x += widths[i]
            }
            y += height
            canvas.fillRect(MARGIN, y, CONTENT_WIDTH, LINE, colors.line)
        }

        val headerHeight = rowHeight(block.headers, PdfText.BODY_BOLD)
        ensure(headerHeight + ROW_MIN)
        drawRow(block.headers, PdfText.BODY_BOLD, colors.soft)
        block.rows.forEach { row ->
            if (y + rowHeight(row, PdfText.BODY) > bottom) {
                startPage()
                drawRow(block.headers, PdfText.BODY_BOLD, colors.soft)
            }
            drawRow(row, PdfText.BODY, null)
        }
        y += GAP
    }

    private fun paragraph(block: PdfBlock.Paragraph) {
        val height = canvas.measure(block.text, PdfText.BODY, CONTENT_WIDTH)
        ensure(height)
        canvas.drawText(block.text, PdfText.BODY, colors.ink, MARGIN, y, CONTENT_WIDTH)
        y += height + GAP
    }

    private fun images(block: PdfBlock.Images) {
        block.items.forEach { (title, path) ->
            val (w, h) = canvas.imageSize(path) ?: return@forEach
            startPage()
            val titleHeight = canvas.measure(title, PdfText.HEADING, CONTENT_WIDTH)
            canvas.drawText(title, PdfText.HEADING, colors.ink, MARGIN, y, CONTENT_WIDTH)
            y += titleHeight + GAP
            val maxHeight = bottom - y
            val scale = min(CONTENT_WIDTH / w, maxHeight / h)
            canvas.drawImage(path, MARGIN + (CONTENT_WIDTH - w * scale) / 2, y, w * scale, h * scale)
            y += h * scale + GAP
        }
    }

    companion object {
        const val PAGE_WIDTH = 595.0
        const val PAGE_HEIGHT = 842.0
        const val MARGIN = 40.0
        const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
        private const val GAP = 8.0
        private const val CELL = 4.0
        private const val LINE = 0.5
        private const val PHOTO = 72.0
        private const val FOOTER_SPACE = 28.0
        private const val ROW_MIN = 18.0
    }
}
