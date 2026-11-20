package com.bestid.core.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.PermissionCard
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res
import com.bestid.core.ui.resources.photo_camera_denied_body
import com.bestid.core.ui.resources.photo_camera_denied_title
import com.bestid.core.ui.resources.photo_failed
import com.bestid.core.ui.resources.state_delete_error
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.ui.resources.state_save_error
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** La persona negó la cámara (iOS): enlace a los ajustes del teléfono. */
@Composable
fun CameraDeniedCard(onOpenSettings: () -> Unit) {
    PermissionCard(
        title = stringResource(Res.string.photo_camera_denied_title),
        body = stringResource(Res.string.photo_camera_denied_body),
        onOpenSettings = onOpenSettings,
    )
}

/** Algunas fotos no se pudieron abrir (formato desconocido o archivo dañado). */
@Composable
fun PhotoFailedCard(count: Int) {
    AlertCard { Text(pluralStringResource(Res.plurals.photo_failed, count, count), style = BestIdTheme.typography.body) }
}

/** No se pudo guardar: el formulario sigue abierto para intentarlo de nuevo. */
@Composable
fun SaveFailedCard() {
    AlertCard { Text(stringResource(Res.string.state_save_error), style = BestIdTheme.typography.body) }
}

/** No se pudo eliminar: el dato sigue ahí y se puede intentar de nuevo. */
@Composable
fun DeleteFailedCard() {
    AlertCard { Text(stringResource(Res.string.state_delete_error), style = BestIdTheme.typography.body) }
}

/** No se pudieron leer los datos de un formulario o una pantalla de detalle. */
@Composable
fun LoadFailedState(onRetry: () -> Unit) {
    ErrorState(stringResource(Res.string.state_load_error), onRetry = onRetry)
}
