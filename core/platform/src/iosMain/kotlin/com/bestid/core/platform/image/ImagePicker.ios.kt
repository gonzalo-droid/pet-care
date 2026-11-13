package com.bestid.core.platform.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify

/**
 * Cámara con `UIImagePickerController` y galería con `PHPickerViewController`, que no pide permiso
 * de fotos. La cámara sí pide permiso (texto en Info.plist); si se niega, avisa con `CameraDenied`.
 */
@Composable
internal actual fun rememberPlatformImagePickerLauncher(onResult: (ImagePickerResult) -> Unit): ImagePickerLauncher {
    val callback by rememberUpdatedState(onResult)
    return remember {
        IosImagePickerLauncher { callback(it) }
    }
}

private class IosImagePickerLauncher(private val onResult: (ImagePickerResult) -> Unit) : ImagePickerLauncher {
    // Los delegados son referencias débiles en UIKit: se guardan aquí mientras la hoja está abierta.
    private var cameraDelegate: CameraDelegate? = null
    private var galleryDelegate: GalleryDelegate? = null

    override val canTakePhoto: Boolean =
        UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)

    override fun takePhoto() {
        if (!canTakePhoto) return
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> presentCamera()
            AVAuthorizationStatusNotDetermined -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                dispatch_async(dispatch_get_main_queue()) {
                    if (granted) presentCamera() else onResult(ImagePickerResult.CameraDenied)
                }
            }
            else -> onResult(ImagePickerResult.CameraDenied)
        }
    }

    private fun presentCamera() {
        val presenter = topViewController() ?: return
        val delegate = CameraDelegate { image ->
            cameraDelegate = null
            image?.let { onResult(ImagePickerResult.Picked(listOf(it))) }
        }
        cameraDelegate = delegate
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    override fun pickFromGallery(max: Int) {
        val presenter = topViewController() ?: return
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = max.coerceAtLeast(1).toLong()
            filter = PHPickerFilter.imagesFilter
        }
        val delegate = GalleryDelegate { images ->
            galleryDelegate = null
            if (images.isNotEmpty()) onResult(ImagePickerResult.Picked(images))
        }
        galleryDelegate = delegate
        val picker = PHPickerViewController(configuration = configuration)
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }
}

private class CameraDelegate(private val onDone: (ByteArray?) -> Unit) :
    NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        // La foto de la cámara llega como UIImage; se pasa a JPEG de máxima calidad y ImageProcessor hace el resto.
        val data = image?.let { UIImageJPEGRepresentation(it, 1.0) }
        picker.dismissViewControllerAnimated(true, completion = null)
        onDone(data?.toByteArray())
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onDone(null)
    }
}

private class GalleryDelegate(private val onDone: (List<ByteArray>) -> Unit) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            onDone(emptyList())
            return
        }
        // Cada imagen llega por separado y en otro hilo; se juntan en orden y se entregan en el principal.
        val loaded = arrayOfNulls<ByteArray>(results.size)
        val group = dispatch_group_create()
        results.forEachIndexed { index, result ->
            dispatch_group_enter(group)
            result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data: NSData?, _ ->
                loaded[index] = data?.toByteArray()
                dispatch_group_leave(group)
            }
        }
        dispatch_group_notify(group, dispatch_get_main_queue()) {
            onDone(loaded.filterNotNull())
        }
    }
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    while (true) top = top.presentedViewController ?: return top
}

@Composable
actual fun rememberOpenAppSettings(): () -> Unit = remember {
    {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
    }
}
