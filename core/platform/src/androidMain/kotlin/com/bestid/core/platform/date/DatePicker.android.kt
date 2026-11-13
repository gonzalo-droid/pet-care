package com.bestid.core.platform.date

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
actual fun rememberDatePickerLauncher(onPicked: (LocalDate) -> Unit): DatePickerLauncher {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onPicked)
    return remember(context) {
        object : DatePickerLauncher {
            override fun launch(initial: LocalDate?, max: LocalDate?) {
                val zone = TimeZone.currentSystemDefault()
                val start = initial ?: Clock.System.todayIn(zone)
                val dialog = DatePickerDialog(
                    context,
                    { _, year, month, day -> callback(LocalDate(year, month + 1, day)) },
                    start.year,
                    start.month.number - 1,
                    start.day,
                )
                if (max != null) {
                    dialog.datePicker.maxDate = max.atStartOfDayIn(zone).toEpochMilliseconds()
                }
                dialog.show()
            }
        }
    }
}
