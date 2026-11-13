package com.bestid.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bestid.core.platform.notification.ReminderTaps
import com.bestid.core.platform.notification.handle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Tema claro forzado: barras del sistema con íconos oscuros aunque el teléfono esté en modo oscuro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        ReminderTaps.handle(intent)
        setContent { App() }
    }

    /** La app ya estaba abierta y se tocó un aviso. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ReminderTaps.handle(intent)
    }
}
