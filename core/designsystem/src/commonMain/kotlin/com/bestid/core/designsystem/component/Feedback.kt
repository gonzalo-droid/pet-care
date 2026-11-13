package com.bestid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_cancel
import com.bestid.core.designsystem.resources.ds_loading
import com.bestid.core.designsystem.resources.ds_notification_app
import com.bestid.core.designsystem.resources.ds_notification_now
import com.bestid.core.designsystem.resources.ds_open_settings
import com.bestid.core.designsystem.resources.ds_retry
import com.bestid.core.designsystem.theme.BestIdTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Estado vacío: ilustración, título, texto y una acción opcional. */
@Composable
fun EmptyState(
    illustration: DrawableResource,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = BestIdTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(BestIdTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier.size(BestIdTheme.sizes.avatarLarge).background(colors.primarySoft, BestIdTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(illustration),
                contentDescription = null,
                tint = colors.primaryInk,
                modifier = Modifier.size(BestIdTheme.sizes.avatarMedium),
            )
        }
        Text(title, style = BestIdTheme.typography.subtitle, color = colors.ink, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        Text(body, style = BestIdTheme.typography.body, color = colors.muted, textAlign = TextAlign.Center)
        if (actionText != null && onAction != null) {
            PrimaryButton(text = actionText, onClick = onAction)
        }
    }
}

/** Estado de carga con texto (nunca solo un indicador). */
@Composable
fun LoadingState(modifier: Modifier = Modifier, text: String? = null) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(BestIdTheme.spacing.xl)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
    ) {
        CircularProgressIndicator(color = BestIdTheme.colors.primary, trackColor = BestIdTheme.colors.track)
        Text(
            text ?: stringResource(Res.string.ds_loading),
            style = BestIdTheme.typography.body,
            color = BestIdTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
    }
}

/** Estado de error con acción para reintentar. */
@Composable
fun ErrorState(text: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(BestIdTheme.spacing.xl)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
    ) {
        Icon(painterResource(BestIdIcons.Error), contentDescription = null, tint = BestIdTheme.colors.error, modifier = Modifier.size(BestIdTheme.sizes.avatarSmall))
        Text(text, style = BestIdTheme.typography.body, color = BestIdTheme.colors.ink, textAlign = TextAlign.Center)
        if (onRetry != null) {
            SecondaryButton(text = stringResource(Res.string.ds_retry), onClick = onRetry)
        }
    }
}

/** Permiso denegado (cámara o avisos): explica y lleva a los ajustes del sistema. */
@Composable
fun PermissionCard(title: String, body: String, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    AlertCard(modifier = modifier, icon = BestIdIcons.Notifications) {
        Text(title, style = BestIdTheme.typography.body, color = BestIdTheme.colors.alertInk)
        Text(body, style = BestIdTheme.typography.support, color = BestIdTheme.colors.alertInk)
        SecondaryButton(text = stringResource(Res.string.ds_open_settings), onClick = onOpenSettings)
    }
}

/** Confirmación antes de eliminar (hoja inferior). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSheet(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BestIdTheme.colors.bg,
        shape = BestIdTheme.shapes.sheet,
    ) {
        ConfirmContent(title, body, confirmText, destructive, onConfirm, onDismiss)
    }
}

@Composable
private fun ConfirmContent(
    title: String,
    body: String,
    confirmText: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
    ) {
        Text(title, style = BestIdTheme.typography.subtitle, modifier = Modifier.semantics { heading() })
        Text(body, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
        PrimaryButton(
            text = confirmText,
            onClick = onConfirm,
            icon = if (destructive) BestIdIcons.Delete else null,
        )
        SecondaryButton(text = stringResource(Res.string.ds_cancel), onClick = onDismiss)
    }
}

/** Opción de [OptionsSheet]. */
data class SheetOption(
    val label: String,
    val icon: DrawableResource? = null,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Hoja inferior con opciones (por ejemplo "Tomar foto", "Elegir de galería", "Quitar foto").
 * Al elegir una, la hoja se cierra y luego se ejecuta la opción.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsSheet(title: String, options: List<SheetOption>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BestIdTheme.colors.bg,
        shape = BestIdTheme.shapes.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
        ) {
            Text(title, style = BestIdTheme.typography.subtitle, modifier = Modifier.semantics { heading() })
            options.forEach { option ->
                val onClick = {
                    onDismiss()
                    option.onClick()
                }
                if (option.destructive) {
                    TextAction(option.label, onClick = onClick, icon = option.icon, destructive = true, modifier = Modifier.fillMaxWidth())
                } else {
                    SecondaryButton(option.label, onClick = onClick, icon = option.icon)
                }
            }
            SecondaryButton(text = stringResource(Res.string.ds_cancel), onClick = onDismiss)
        }
    }
}

/** Duración del snackbar "Deshacer" (sección 4). */
const val UNDO_DURATION_MS: Long = 5_000

/**
 * Muestra un snackbar con "Deshacer" durante 5 segundos.
 * Devuelve `true` si la persona tocó "Deshacer".
 */
suspend fun SnackbarHostState.showUndo(message: String, undoLabel: String): Boolean = try {
    withTimeout(UNDO_DURATION_MS) {
        showSnackbar(
            message = message,
            actionLabel = undoLabel,
            withDismissAction = false,
            duration = SnackbarDuration.Indefinite,
        ) == SnackbarResult.ActionPerformed
    }
} catch (_: TimeoutCancellationException) {
    false
}

/** Apariencia del snackbar "Deshacer". Se usa como contenido de `SnackbarHost`. */
@Composable
fun UndoSnackbar(data: SnackbarData, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    Snackbar(
        snackbarData = data,
        modifier = modifier.padding(BestIdTheme.spacing.md),
        shape = BestIdTheme.shapes.field,
        containerColor = colors.ink,
        contentColor = colors.surface,
        actionColor = colors.primarySoft,
    )
}

/** Ejemplo del aviso en la pantalla de permiso (O5). */
@Composable
fun NotificationPreview(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    PlainCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            Box(
                Modifier.size(BestIdTheme.sizes.icon).background(colors.primary, BestIdTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(BestIdIcons.PetsFilled), null, tint = colors.onPrimary, modifier = Modifier.size(BestIdTheme.sizes.iconSmall))
            }
            Text(stringResource(Res.string.ds_notification_app), style = BestIdTheme.typography.label, color = colors.muted, modifier = Modifier.weight(1f))
            Text(stringResource(Res.string.ds_notification_now), style = BestIdTheme.typography.label, color = colors.muted)
        }
        Text(title, style = BestIdTheme.typography.body, color = colors.ink)
        Text(body, style = BestIdTheme.typography.support, color = colors.ink)
    }
}

/** Confirmación tierna después de guardar ("Lilith y sus 2 vacunas guardadas"). */
@Composable
fun CelebrationBadge(text: String, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    Row(
        modifier = modifier
            .heightIn(min = BestIdTheme.sizes.minTouch)
            .background(colors.primarySoft, BestIdTheme.shapes.pill)
            .padding(horizontal = BestIdTheme.spacing.lg, vertical = BestIdTheme.spacing.sm)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        Icon(painterResource(BestIdIcons.Celebration), contentDescription = null, tint = colors.primaryInk)
        Text(text, style = BestIdTheme.typography.body, color = colors.primaryInk)
    }
}

@Preview
@Composable
private fun FeedbackPreview() = PreviewSurface {
    EmptyState(BestIdIcons.Vaccines, PreviewData.EMPTY_TITLE, PreviewData.EMPTY_BODY, actionText = PreviewData.EMPTY_ACTION, onAction = {})
    LoadingState()
    NotificationPreview(PreviewData.NOTIFICATION_TITLE, PreviewData.NOTIFICATION_BODY)
    CelebrationBadge(PreviewData.INFO)
}

@Preview
@Composable
private fun FeedbackErrorPreview() = PreviewSurface {
    ErrorState(PreviewData.ERROR, onRetry = {})
    PermissionCard(PreviewData.PERMISSION_TITLE, PreviewData.PERMISSION_BODY, onOpenSettings = {})
    ConfirmContent(PreviewData.CONFIRM_TITLE, PreviewData.CONFIRM_BODY, PreviewData.CONFIRM_ACTION, true, {}, {})
}

@Preview(fontScale = 2f)
@Composable
private fun FeedbackLongTextPreview() = PreviewSurface {
    EmptyState(BestIdIcons.Pets, PreviewData.LONG_TEXT, PreviewData.LONG_TEXT)
    CelebrationBadge(PreviewData.LONG_TEXT)
    PermissionCard(PreviewData.LONG_TEXT, PreviewData.LONG_TEXT, onOpenSettings = {})
}
