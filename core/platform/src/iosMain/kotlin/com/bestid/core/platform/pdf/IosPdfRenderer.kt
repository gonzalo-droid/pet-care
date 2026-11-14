package com.bestid.core.platform.pdf

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererContext
import platform.UIKit.UIGraphicsPDFRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIRectFill
import platform.UIKit.boundingRectWithSize
import platform.UIKit.drawInRect
import platform.UIKit.drawWithRect
import kotlin.math.ceil

/** `PdfRenderer` con `UIGraphicsPDFRenderer` (sección 8). El diseño lo pone [PdfLayout]. */
@OptIn(ExperimentalForeignApi::class)
class IosPdfRenderer : PdfRenderer {
    override suspend fun render(spec: PdfDocumentSpec, target: String) = withContext(Dispatchers.IO) {
        val url = NSURL.fileURLWithPath(target)
        url.URLByDeletingLastPathComponent?.path?.let {
            NSFileManager.defaultManager.createDirectoryAtPath(it, withIntermediateDirectories = true, attributes = null, error = null)
        }
        val renderer = UIGraphicsPDFRenderer(
            bounds = CGRectMake(0.0, 0.0, PdfLayout.PAGE_WIDTH, PdfLayout.PAGE_HEIGHT),
            format = UIGraphicsPDFRendererFormat(),
        )
        val ok = renderer.writePDFToURL(url, withActions = { context ->
            val canvas = IosPdfCanvas(requireNotNull(context))
            context.beginPage()
            PdfLayout(canvas, spec).render()
        }, error = null)
        check(ok) { "No se pudo escribir el PDF" }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosPdfCanvas(private val context: UIGraphicsPDFRendererContext) : PdfCanvas {
    private fun attributes(style: PdfText, color: Int = 0xFF000000.toInt()): Map<Any?, *> = mapOf(
        NSFontAttributeName to if (style.bold) UIFont.boldSystemFontOfSize(style.size) else UIFont.systemFontOfSize(style.size),
        NSForegroundColorAttributeName to color.toUIColor(),
    )

    private fun String.ns(): NSString = NSString.create(string = this)

    override fun measure(text: String, style: PdfText, width: Double): Double =
        text.ns().boundingRectWithSize(
            CGSizeMake(width, Double.MAX_VALUE),
            options = NSStringDrawingUsesLineFragmentOrigin,
            attributes = attributes(style),
            context = null,
        ).useContents { ceil(size.height) }

    override fun drawText(text: String, style: PdfText, color: Int, x: Double, y: Double, width: Double) {
        val height = measure(text, style, width)
        text.ns().drawWithRect(CGRectMake(x, y, width, height), options = NSStringDrawingUsesLineFragmentOrigin, attributes = attributes(style, color), context = null)
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double, color: Int) {
        color.toUIColor().setFill()
        UIRectFill(CGRectMake(x, y, width, height))
    }

    override fun imageSize(path: String): Pair<Int, Int>? {
        val image = UIImage.imageWithContentsOfFile(path) ?: return null
        return image.size.useContents { (width * image.scale).toInt() to (height * image.scale).toInt() }.takeIf { it.first > 0 && it.second > 0 }
    }

    override fun drawImage(path: String, x: Double, y: Double, width: Double, height: Double) {
        UIImage.imageWithContentsOfFile(path)?.drawInRect(CGRectMake(x, y, width, height))
    }

    override fun newPage() = context.beginPage()
}

private fun Int.toUIColor(): UIColor = UIColor(
    red = ((this shr 16) and 0xFF) / 255.0,
    green = ((this shr 8) and 0xFF) / 255.0,
    blue = (this and 0xFF) / 255.0,
    alpha = ((this ushr 24) and 0xFF) / 255.0,
)
