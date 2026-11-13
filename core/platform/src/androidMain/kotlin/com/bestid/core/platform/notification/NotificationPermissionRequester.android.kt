package com.bestid.core.platform.notification

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat

@Composable
actual fun rememberNotificationPermissionRequester(onResult: (granted: Boolean) -> Unit): NotificationPermissionRequester {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> callback(granted) }
    return remember(context, launcher) {
        object : NotificationPermissionRequester {
            override fun request() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionPrefs.markAsked(context)
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Antes de Android 13 no hay diálogo: los avisos están permitidos salvo que se apaguen en ajustes.
                    callback(NotificationManagerCompat.from(context).areNotificationsEnabled())
                }
            }

            override fun openSettings() {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }
}
