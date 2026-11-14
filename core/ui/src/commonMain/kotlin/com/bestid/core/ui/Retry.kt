package com.bestid.core.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Estado de error con "Reintentar" (sección 4): si el flujo de una pantalla falla (por ejemplo, la
 * base no se puede leer), emite [error] en lugar de cerrar la app; [retry] lo vuelve a empezar.
 */
class Retry {
    private val attempts = MutableStateFlow(0)

    fun retry() {
        attempts.value++
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> wrap(error: T, flow: () -> Flow<T>): Flow<T> = attempts.flatMapLatest {
        flow().catch { e ->
            if (e is CancellationException) throw e
            emit(error)
        }
    }
}

/** Ejecuta una acción de guardado o borrado; `false` si falló (para mostrar "No pudimos guardar"). */
suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
