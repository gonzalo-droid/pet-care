package com.bestid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_accept
import com.bestid.core.designsystem.resources.ds_cancel
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.designsystem.theme.isLargeFont
import org.jetbrains.compose.resources.stringResource

/** Diálogo para elegir una hora (hora de los avisos). Reloj de 12 horas, como se usa en Perú. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    val c = BestIdTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.bg,
        shape = BestIdTheme.shapes.card,
        title = { Text(title, style = BestIdTheme.typography.subtitle, color = c.ink, modifier = Modifier.semantics { heading() }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
                val pickerColors = TimePickerDefaults.colors(
                        clockDialColor = c.track,
                        selectorColor = c.primary,
                        clockDialSelectedContentColor = c.onPrimary,
                        clockDialUnselectedContentColor = c.ink,
                        periodSelectorBorderColor = c.outlineStrong,
                        periodSelectorSelectedContainerColor = c.primarySoft,
                        periodSelectorSelectedContentColor = c.primaryInk,
                        periodSelectorUnselectedContentColor = c.ink,
                        timeSelectorSelectedContainerColor = c.primarySoft,
                        timeSelectorSelectedContentColor = c.primaryInk,
                        timeSelectorUnselectedContainerColor = c.track,
                        timeSelectorUnselectedContentColor = c.ink,
                )
                // El reloj tiene tamaño fijo y se recorta con letra grande: entonces se escribe la hora.
                if (isLargeFont()) {
                    TimeInput(state = state, colors = pickerColors)
                } else {
                    TimePicker(state = state, colors = pickerColors)
                }
            }
        },
        confirmButton = { TextAction(stringResource(Res.string.ds_accept), onClick = { onConfirm(state.hour, state.minute) }) },
        dismissButton = { TextAction(stringResource(Res.string.ds_cancel), onClick = onDismiss) },
    )
}
