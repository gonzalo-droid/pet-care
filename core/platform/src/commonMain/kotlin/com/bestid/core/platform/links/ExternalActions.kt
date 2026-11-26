package com.bestid.core.platform.links

import androidx.compose.runtime.Composable

/**
 * Acciones que salen de la app (Ajustes, AJU-01..05): correo, compartir un texto, abrir un enlace y la
 * ficha de la tienda para calificar. Si no hay app que las maneje, no pasa nada.
 */
interface ExternalActions {
    /** Modelo y versión del sistema, para prellenar el correo de contacto (AJU-02). */
    val deviceInfo: String

    fun email(to: String, subject: String, body: String)
    fun shareText(text: String)
    fun openUrl(url: String)

    /** Ficha de la app en Play o App Store para dejar una reseña. */
    fun openStoreReview()
}

@Composable
expect fun rememberExternalActions(): ExternalActions
