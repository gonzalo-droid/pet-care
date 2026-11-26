package com.bestid.core.platform.links

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberExternalActions(): ExternalActions = remember { IosExternalActions() }

private class IosExternalActions : ExternalActions {
    override val deviceInfo = UIDevice.currentDevice.run { "$model, iOS $systemVersion" }

    override fun email(to: String, subject: String, body: String) = open("mailto:$to?subject=${subject.encoded()}&body=${body.encoded()}")

    @OptIn(ExperimentalForeignApi::class)
    override fun shareText(text: String) {
        val presenter = topViewController() ?: return
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        // TAB-03: en iPad la hoja es un popover y necesita de dónde salir.
        controller.popoverPresentationController?.let { popover ->
            popover.sourceView = presenter.view
            presenter.view.bounds.useContents { popover.sourceRect = CGRectMake(size.width / 2, size.height / 2, 1.0, 1.0) }
        }
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    override fun openUrl(url: String) = open(url)

    // Sin el ID numérico del App Store (aún no hay ficha pública), el aviso nativo de reseña.
    // shortcut: cuando la app esté en el App Store, abrir itms-apps://apps.apple.com/app/id<ID>?action=write-review.
    override fun openStoreReview() {
        val scene = UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>().firstOrNull() ?: return
        SKStoreReviewController.requestReviewInScene(scene)
    }

    private fun open(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun String.encoded(): String =
        (this as NSString).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet)
            ?.replace("&", "%26")?.replace("+", "%2B") ?: this

    private fun topViewController(): UIViewController? {
        var top = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
        while (true) top = top.presentedViewController ?: return top
    }
}
