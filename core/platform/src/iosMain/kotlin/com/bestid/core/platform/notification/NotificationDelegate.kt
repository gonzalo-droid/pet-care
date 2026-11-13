package com.bestid.core.platform.notification

import kotlinx.cinterop.ObjCSignatureOverride
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

/**
 * Recibe el toque de un aviso (abre el registro) y muestra el aviso aunque la app esté abierta.
 */
private class NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {

    @ObjCSignatureOverride
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit,
    ) {
        val info = didReceiveNotificationResponse.notification.request.content.userInfo
        ReminderTaps.emit(info[ReminderTaps.EXTRA_REF_TYPE] as? String, info[ReminderTaps.EXTRA_REF_ID] as? String)
        withCompletionHandler()
    }

    @ObjCSignatureOverride
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound,
        )
    }
}

/** El centro guarda el delegado como referencia débil: este objeto lo retiene. */
object IosNotifications {
    private var delegate: NotificationDelegate? = null

    /** Llamar al iniciar la app, antes de que termine de abrir (para recibir el toque que la abrió). */
    fun installDelegate() {
        val instance = delegate ?: NotificationDelegate().also { delegate = it }
        UNUserNotificationCenter.currentNotificationCenter().delegate = instance
    }
}
