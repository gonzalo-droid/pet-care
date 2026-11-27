package com.bestid.core.platform.links

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
actual fun rememberExternalActions(): ExternalActions {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidExternalActions(context) }
}

/** Intents implícitos desde el contexto de aplicación (por eso FLAG_ACTIVITY_NEW_TASK). */
private class AndroidExternalActions(private val context: Context) : ExternalActions {
    override val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}"

    override fun email(to: String, subject: String, body: String) {
        // TRP-07: Gmail no lee el destinatario ni el asunto de la URI: van también en los extras.
        launch(
            Intent(Intent.ACTION_SENDTO, "mailto:$to".toUri())
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                .putExtra(Intent.EXTRA_SUBJECT, subject)
                .putExtra(Intent.EXTRA_TEXT, body),
        )
    }

    override fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        launch(Intent.createChooser(send, null))
    }

    override fun openUrl(url: String) {
        launch(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    override fun openStoreReview() {
        val pkg = context.packageName
        if (!launch(Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()))) {
            openUrl("https://play.google.com/store/apps/details?id=$pkg")
        }
    }

    private fun launch(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
