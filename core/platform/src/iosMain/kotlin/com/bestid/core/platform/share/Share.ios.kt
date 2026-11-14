package com.bestid.core.platform.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.UniformTypeIdentifiers.UTTypeZIP
import platform.darwin.NSObject

/** `UIActivityViewController` con el archivo (incluye WhatsApp y "Guardar en Archivos"). */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberPlatformShareSheet(): ShareSheet = remember {
    object : ShareSheet {
        override fun share(path: String, mimeType: String, title: String) {
            val presenter = topViewController() ?: return
            val controller = UIActivityViewController(activityItems = listOf(NSURL.fileURLWithPath(path)), applicationActivities = null)
            // En iPad la hoja es un popover y necesita de dónde salir.
            controller.popoverPresentationController?.let { popover ->
                popover.sourceView = presenter.view
                presenter.view.bounds.useContents { popover.sourceRect = CGRectMake(size.width / 2, size.height / 2, 1.0, 1.0) }
            }
            presenter.presentViewController(controller, animated = true, completion = null)
        }
    }
}

/** `UIDocumentPickerViewController` en modo copia: el archivo llega a la carpeta temporal de la app. */
@Composable
internal actual fun rememberPlatformFilePicker(onPicked: (String) -> Unit): FilePicker {
    val callback by rememberUpdatedState(onPicked)
    return remember {
        object : FilePicker {
            // UIKit guarda el delegado como referencia débil.
            private var delegate: PickerDelegate? = null

            override fun pick() {
                val presenter = topViewController() ?: return
                val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeZIP), asCopy = true)
                val d = PickerDelegate { path ->
                    delegate = null
                    path?.let { callback(it) }
                }
                delegate = d
                picker.delegate = d
                presenter.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

private class PickerDelegate(private val onDone: (String?) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onDone(url?.let(::copyToTemp))
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onDone(null)
}

/** El sistema puede borrar la copia del selector; se lleva a un nombre fijo en la carpeta temporal. */
@OptIn(ExperimentalForeignApi::class)
private fun copyToTemp(url: NSURL): String? {
    val source = url.path ?: return null
    val target = NSTemporaryDirectory() + "importar.zip"
    val files = NSFileManager.defaultManager
    files.removeItemAtPath(target, error = null)
    return if (files.copyItemAtPath(source, toPath = target, error = null)) target else source
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    while (true) top = top.presentedViewController ?: return top
}
