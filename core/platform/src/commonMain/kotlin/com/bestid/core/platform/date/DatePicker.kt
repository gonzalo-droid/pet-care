package com.bestid.core.platform.date

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

/**
 * Puerto del selector de fecha nativo (D14): `DatePickerDialog` en Android y `UIDatePicker` en iOS.
 * Si la persona cancela, no se llama a `onPicked`.
 */
interface DatePickerLauncher {
    fun launch(initial: LocalDate?, max: LocalDate? = null)
}

@Composable
expect fun rememberDatePickerLauncher(onPicked: (LocalDate) -> Unit): DatePickerLauncher
