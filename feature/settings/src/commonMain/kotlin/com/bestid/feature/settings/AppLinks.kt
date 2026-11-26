package com.bestid.feature.settings

/** Enlaces de Ajustes (AJU-01..04). Las páginas viven en el sitio de Nekodojo (NEK-01, NEK-02). */
internal object AppLinks {
    private const val BASE = "https://nekodojo.app/apps/bestid"
    const val PRIVACY = "$BASE/privacy"
    const val TERMS = "$BASE/terms"
    /** Redirige a la tienda de cada teléfono (AJU-04). */
    const val DOWNLOAD = "$BASE/descargar"
    const val SUPPORT_EMAIL = "contacto@gondroid.dev"
}
