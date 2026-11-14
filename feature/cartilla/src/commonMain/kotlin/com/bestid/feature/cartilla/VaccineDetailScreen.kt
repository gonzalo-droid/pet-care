package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.Badge
import com.bestid.core.designsystem.component.BadgeTone
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.PhotoThumb
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.domain.usecase.RestoreVaccineRecord
import com.bestid.core.ui.daysText
import com.bestid.core.ui.formatLong
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.cartilla_in_days
import com.bestid.feature.cartilla.resources.cartilla_next_line
import com.bestid.feature.cartilla.resources.cartilla_overdue
import com.bestid.feature.cartilla.resources.cartilla_to_confirm
import com.bestid.feature.cartilla.resources.cartilla_today
import com.bestid.feature.cartilla.resources.cartilla_vaccine_fallback
import com.bestid.feature.cartilla.resources.detail_add
import com.bestid.feature.cartilla.resources.detail_back
import com.bestid.feature.cartilla.resources.detail_complete_date
import com.bestid.feature.cartilla.resources.detail_delete
import com.bestid.feature.cartilla.resources.detail_delete_body
import com.bestid.feature.cartilla.resources.detail_delete_confirm
import com.bestid.feature.cartilla.resources.detail_delete_title
import com.bestid.feature.cartilla.resources.detail_deleted
import com.bestid.feature.cartilla.resources.detail_edit
import com.bestid.feature.cartilla.resources.detail_next_dose
import com.bestid.feature.cartilla.resources.detail_next_missing
import com.bestid.feature.cartilla.resources.detail_next_missing_body
import com.bestid.feature.cartilla.resources.detail_not_found
import com.bestid.feature.cartilla.resources.detail_not_registered
import com.bestid.feature.cartilla.resources.detail_product_unknown
import com.bestid.feature.cartilla.resources.detail_add_sticker_photo
import com.bestid.feature.cartilla.resources.detail_photos
import com.bestid.feature.cartilla.resources.detail_sticker_expires
import com.bestid.feature.cartilla.resources.photo_page
import com.bestid.feature.cartilla.resources.photo_page_description
import com.bestid.feature.cartilla.resources.photo_sticker
import com.bestid.feature.cartilla.resources.photo_sticker_description
import com.bestid.feature.cartilla.resources.detail_sticker_line
import com.bestid.feature.cartilla.resources.detail_sticker_made
import com.bestid.feature.cartilla.resources.detail_stickers
import com.bestid.feature.cartilla.resources.detail_title
import com.bestid.feature.cartilla.resources.detail_undo
import com.bestid.feature.cartilla.resources.detail_vet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Detalle de vacuna (C2): fecha, tipos, próxima dosis, stickers, veterinaria, editar y eliminar. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaccineDetailScreen(
    recordId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenPhoto: (String) -> Unit,
    viewModel: VaccineDetailViewModel = koinViewModel(key = "vaccine-detail-$recordId") { parametersOf(recordId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val undo = LocalUndoController.current
    val restore = koinInject<RestoreVaccineRecord>()
    val deletedMessage = stringResource(Res.string.detail_deleted)
    val undoLabel = stringResource(Res.string.detail_undo)
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.deletedId) {
        state.deletedId?.let { id ->
            undo.show(deletedMessage, undoLabel) { restore(id) }
            onBack()
        }
    }

    val record = state.record
    FlowScaffold(
        title = null,
        onClose = onBack,
        closeIcon = BestIdIcons.ArrowBack,
        closeDescription = stringResource(Res.string.detail_back),
        actions = {
            if (record != null) TextAction(stringResource(Res.string.detail_edit), onClick = { onEdit(record.id) })
        },
    ) {
        when {
            state.loading -> LoadingState()
            state.failed -> ErrorState(stringResource(UiRes.string.state_load_error), onRetry = viewModel::retry)
            record == null -> ErrorState(stringResource(Res.string.detail_not_found))
            else -> {
                state.pet?.let {
                    Text(stringResource(Res.string.detail_title, it.name), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                }
                Text(
                    record.applicationDate.formatLong(),
                    style = BestIdTheme.typography.display,
                    modifier = Modifier.semantics { heading() },
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                ) {
                    record.typeIds.labels(state.types).forEach { Badge(it, BadgeTone.OK) }
                    record.typeIdsToConfirm.labels(state.types).forEach {
                        Badge(stringResource(Res.string.cartilla_to_confirm, it), BadgeTone.ALERT)
                    }
                }

                val nextName = record.nextDateLabel
                    ?: record.typeIds.labels(state.types).firstOrNull()
                    ?: stringResource(Res.string.cartilla_vaccine_fallback)
                val nextDate = record.nextDate
                val days = state.daysLeft
                when {
                    nextDate != null && days != null -> {
                        val whenText = when {
                            days == 0 -> stringResource(Res.string.cartilla_today)
                            days > 0 -> stringResource(Res.string.cartilla_in_days, daysText(days))
                            else -> stringResource(Res.string.cartilla_overdue, daysText(-days))
                        }
                        val body: @Composable () -> Unit = {
                            Eyebrow(stringResource(Res.string.detail_next_dose), color = if (days <= 7) BestIdTheme.colors.alertInk else BestIdTheme.colors.primaryInk)
                            Text(stringResource(Res.string.cartilla_next_line, nextName, nextDate.formatLong()), style = BestIdTheme.typography.card)
                            Text(whenText, style = BestIdTheme.typography.body)
                        }
                        if (days <= 7) AlertCard { body() } else InfoCard(icon = BestIdIcons.Calendar) { body() }
                    }
                    record.nextDateLabel != null -> AlertCard {
                        Eyebrow(stringResource(Res.string.detail_next_dose), color = BestIdTheme.colors.alertInk)
                        Text(stringResource(Res.string.cartilla_next_line, nextName, stringResource(Res.string.detail_next_missing)), style = BestIdTheme.typography.card)
                        Text(stringResource(Res.string.detail_next_missing_body), style = BestIdTheme.typography.body)
                        SecondaryButton(stringResource(Res.string.detail_complete_date), onClick = { onEdit(record.id) })
                    }
                }

                if (record.items.isNotEmpty()) {
                    PlainCard {
                        Eyebrow(stringResource(Res.string.detail_stickers))
                        record.items.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(color = BestIdTheme.colors.line)
                            Text(
                                item.productName ?: stringResource(Res.string.detail_product_unknown),
                                style = BestIdTheme.typography.bodyStrong,
                            )
                            val parts = listOfNotNull(
                                item.lot?.let { stringResource(Res.string.detail_sticker_line, it) },
                                item.manufactureDate?.let { stringResource(Res.string.detail_sticker_made, it.formatLong()) },
                                item.expiryDate?.let { stringResource(Res.string.detail_sticker_expires, it.formatLong()) },
                            )
                            if (parts.isNotEmpty()) {
                                Text(parts.joinToString(" · "), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                            }
                        }
                    }
                }

                // Foto original siempre visible (sección 2): páginas de la cartilla y sticker.
                val sticker = state.stickerPhoto
                if (state.pages.isNotEmpty() || sticker != null) {
                    PlainCard {
                        Eyebrow(stringResource(Res.string.detail_photos))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                        ) {
                            state.pages.forEachIndexed { index, page ->
                                LabeledThumb(page.path, stringResource(Res.string.photo_page, index + 1), stringResource(Res.string.photo_page_description, index + 1), onOpenPhoto)
                            }
                            if (sticker != null) {
                                LabeledThumb(sticker.path, stringResource(Res.string.photo_sticker), stringResource(Res.string.photo_sticker_description), onOpenPhoto)
                            }
                        }
                        if (sticker == null) {
                            TextAction(stringResource(Res.string.detail_add_sticker_photo), onClick = { onEdit(record.id) }, icon = BestIdIcons.PhotoCamera)
                        }
                    }
                }

                PlainCard {
                    Eyebrow(stringResource(Res.string.detail_vet))
                    val vet = state.vet
                    if (vet == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(Res.string.detail_not_registered),
                                style = BestIdTheme.typography.body,
                                color = BestIdTheme.colors.muted,
                                modifier = Modifier.weight(1f),
                            )
                            TextAction(stringResource(Res.string.detail_add), onClick = { onEdit(record.id) })
                        }
                    } else {
                        Text(vet.name, style = BestIdTheme.typography.bodyStrong)
                        vet.contact?.let { Text(it, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted) }
                    }
                }

                TextAction(
                    text = stringResource(Res.string.detail_delete),
                    onClick = { confirmDelete = true },
                    icon = BestIdIcons.Delete,
                    destructive = true,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }

    if (confirmDelete) {
        ConfirmSheet(
            title = stringResource(Res.string.detail_delete_title),
            body = stringResource(Res.string.detail_delete_body, state.pet?.name.orEmpty()),
            confirmText = stringResource(Res.string.detail_delete_confirm),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun LabeledThumb(path: String, label: String, description: String, onOpen: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs)) {
        PhotoThumb(image = path, onClick = { onOpen(path) }, contentDescription = description)
        Text(label, style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
    }
}
