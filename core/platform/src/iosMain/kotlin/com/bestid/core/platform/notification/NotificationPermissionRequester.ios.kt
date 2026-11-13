package com.bestid.core.platform.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberNotificationPermissionRequester(onResult: (granted: Boolean) -> Unit): NotificationPermissionRequester {
    val callback by rememberUpdatedState(onResult)
    return remember {
        object : NotificationPermissionRequester {
            override fun request() {
                UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                    UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
                ) { granted, _ -> dispatch_async(dispatch_get_main_queue()) { callback(granted) } }
            }

            override fun openSettings() {
                val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
                UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
            }
        }
    }
}
