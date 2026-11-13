package com.bestid.core.platform.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.LocalDate

/**
 * Puerto del selector de fecha nativo (D14): `DatePickerDialog` en Android y `UIDatePicker` en iOS.
 * Si la persona cancela, no se llama a `onPicked`.
 */
interface DatePickerLauncher {
    fun launch(initial: LocalDate?, max: LocalDate? = null)
}

/** Reemplazo del selector nativo, solo para pruebas de UI (el selector nativo no se puede tocar desde Compose). */
fun interface DatePickerOverride {
    fun pick(initial: LocalDate?): LocalDate?
}

val LocalDatePickerOverride = staticCompositionLocalOf<DatePickerOverride?> { null }

@Composable
fun rememberDatePickerLauncher(onPicked: (LocalDate) -> Unit): DatePickerLauncher {
    val override = LocalDatePickerOverride.current ?: return rememberPlatformDatePickerLauncher(onPicked)
    val callback by rememberUpdatedState(onPicked)
    return remember(override) {
        object : DatePickerLauncher {
            override fun launch(initial: LocalDate?, max: LocalDate?) {
                override.pick(initial)?.let(callback)
            }
        }
    }
}

@Composable
internal expect fun rememberPlatformDatePickerLauncher(onPicked: (LocalDate) -> Unit): DatePickerLauncher
