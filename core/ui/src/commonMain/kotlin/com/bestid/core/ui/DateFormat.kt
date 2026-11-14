package com.bestid.core.ui

import androidx.compose.runtime.Composable
import com.bestid.core.ui.resources.Res
import com.bestid.core.ui.resources.days_count
import com.bestid.core.ui.resources.vaccines_registered
import com.bestid.core.ui.resources.months_short
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.getStringArray
import org.jetbrains.compose.resources.stringArrayResource

/** "21 dic 2025" (sección 15: fechas legibles en es-PE). */
@Composable
fun LocalDate.formatLong(): String = "$day ${monthShort()} $year"

/** "21 dic 2025" fuera de la composición (por ejemplo, al armar el PDF en un ViewModel). */
suspend fun LocalDate.formatLongText(): String = "$day ${getStringArray(Res.array.months_short)[month.number - 1]} $year"

/** "31 dic" */
@Composable
fun LocalDate.formatDayMonth(): String = "$day ${monthShort()}"

/** "21/12/2025" (dd/MM/aaaa), para PDF y respaldo. */
fun LocalDate.formatNumeric(): String =
    "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}/$year"

/** "1 día", "2 días" */
@Composable
fun daysText(days: Int): String = pluralStringResource(Res.plurals.days_count, days, days)

@Composable
private fun LocalDate.monthShort(): String = stringArrayResource(Res.array.months_short)[month.number - 1]

/** "1 vacuna registrada", "2 vacunas registradas" */
@Composable
fun vaccinesRegisteredText(count: Int): String =
    pluralStringResource(Res.plurals.vaccines_registered, count, count)
