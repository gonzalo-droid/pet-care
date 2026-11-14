package com.bestid.core.domain.port

/**
 * Lectura de la cartilla (sección 10). En M3 solo se declara: la tecnología se elige más adelante
 * (D4, D29) y mientras tanto las vacunas se registran a mano viendo la foto.
 */
interface CardReader {
    /** Envía las páginas (JPEG ya procesados) y devuelve el JSON de la sección 10 tal como llega. */
    suspend fun read(pages: List<ByteArray>): CardReadResult
}

sealed interface CardReadResult {
    data class Read(val json: String) : CardReadResult
    data object NoInternet : CardReadResult
    data class Failed(val reason: String) : CardReadResult
}
