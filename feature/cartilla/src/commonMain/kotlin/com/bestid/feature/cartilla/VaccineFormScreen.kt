package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.ChipToggle
import com.bestid.core.designsystem.component.DashedButton
import com.bestid.core.designsystem.component.DateField
import com.bestid.core.designsystem.component.ExpandableSection
import com.bestid.core.designsystem.component.FieldError
import com.bestid.core.designsystem.component.FieldKind
import com.bestid.core.designsystem.component.FieldLabelText
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.FormField
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PhotoThumb
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.SectionTitle
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.validation.VaccineError
import com.bestid.core.platform.date.rememberDatePickerLauncher
import com.bestid.core.platform.image.MAX_CARD_PAGES
import com.bestid.core.platform.image.rememberImagePickerLauncher
import com.bestid.core.platform.image.rememberOpenAppSettings
import com.bestid.core.ui.CameraDeniedCard
import com.bestid.core.ui.PhotoFailedCard
import com.bestid.core.ui.formatLong
import kotlinx.datetime.LocalDate
import com.bestid.core.ui.label
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.form_add_page
import com.bestid.feature.cartilla.resources.form_add_sticker
import com.bestid.feature.cartilla.resources.form_add_sticker_photo
import com.bestid.feature.cartilla.resources.form_pages_hint
import com.bestid.feature.cartilla.resources.form_pages_title
import com.bestid.feature.cartilla.resources.form_sticker_photo
import com.bestid.feature.cartilla.resources.photo_page_description
import com.bestid.feature.cartilla.resources.photo_sticker_description
import com.bestid.feature.cartilla.resources.form_close
import com.bestid.feature.cartilla.resources.form_date
import com.bestid.feature.cartilla.resources.form_edit_title
import com.bestid.feature.cartilla.resources.form_error_date
import com.bestid.feature.cartilla.resources.form_error_expiry
import com.bestid.feature.cartilla.resources.form_error_future
import com.bestid.feature.cartilla.resources.form_error_next
import com.bestid.feature.cartilla.resources.form_error_type
import com.bestid.feature.cartilla.resources.form_expiry
import com.bestid.feature.cartilla.resources.form_lot
import com.bestid.feature.cartilla.resources.form_lot_placeholder
import com.bestid.feature.cartilla.resources.form_manufacture
import com.bestid.feature.cartilla.resources.form_more
import com.bestid.feature.cartilla.resources.form_new_title
import com.bestid.feature.cartilla.resources.form_next_date
import com.bestid.feature.cartilla.resources.form_no_pet
import com.bestid.feature.cartilla.resources.form_product
import com.bestid.feature.cartilla.resources.form_product_placeholder
import com.bestid.feature.cartilla.resources.form_remove_sticker
import com.bestid.feature.cartilla.resources.form_save
import com.bestid.feature.cartilla.resources.form_sticker_title
import com.bestid.feature.cartilla.resources.form_type_other
import com.bestid.feature.cartilla.resources.form_type_other_add
import com.bestid.feature.cartilla.resources.form_type_other_label
import com.bestid.feature.cartilla.resources.form_type_other_placeholder
import com.bestid.feature.cartilla.resources.form_types_all_hint
import com.bestid.feature.cartilla.resources.form_types_for
import com.bestid.feature.cartilla.resources.form_types_question
import com.bestid.feature.cartilla.resources.form_types_show_all
import com.bestid.feature.cartilla.resources.form_types_show_suggested
import com.bestid.feature.cartilla.resources.form_vet
import com.bestid.feature.cartilla.resources.form_vet_contact
import com.bestid.feature.cartilla.resources.form_vet_contact_placeholder
import com.bestid.feature.cartilla.resources.form_vet_placeholder
import com.bestid.feature.cartilla.resources.form_warning_expired
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Agregar o editar vacuna a mano (C3). Obligatorio: al menos un tipo y la fecha de aplicación.
 * Tipos filtrados por especie con "Ver todas" y "Otra"; el resto en "Más detalles".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaccineFormScreen(
    recordId: String?,
    photoIds: List<String>,
    onClose: () -> Unit,
    onAskPermission: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    viewModel: VaccineFormViewModel = koinViewModel(key = "vaccine-form-$recordId-${photoIds.joinToString()}") {
        parametersOf(recordId, photoIds)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val vets by viewModel.vets.collectAsStateWithLifecycle()
    var dateTarget by remember { mutableStateOf<DateTarget>(DateTarget.Application) }
    val datePicker = rememberDatePickerLauncher { viewModel.onDate(dateTarget, it) }
    fun pick(target: DateTarget, initial: LocalDate?, max: LocalDate? = null) {
        dateTarget = target
        datePicker.launch(initial, max)
    }

    var photoTarget by remember { mutableStateOf(PhotoTarget.PAGE) }
    val picker = rememberImagePickerLauncher { viewModel.onPhotoPicked(photoTarget, it) }
    val openSettings = rememberOpenAppSettings()
    var sheet by remember { mutableStateOf<PhotoSheet?>(null) }

    @Composable
    fun AddPageButton() = DashedButton(
        stringResource(Res.string.form_add_page),
        onClick = {
            photoTarget = PhotoTarget.PAGE
            sheet = PhotoSheet.AddPage
        },
        icon = BestIdIcons.PhotoCamera,
        loading = state.importing && photoTarget == PhotoTarget.PAGE,
    )

    LaunchedEffect(state.done) {
        if (state.done) if (state.askPermission) onAskPermission() else onClose()
    }

    FlowScaffold(
        title = stringResource(if (state.isEdit) Res.string.form_edit_title else Res.string.form_new_title),
        onClose = onClose,
        closeDescription = stringResource(Res.string.form_close),
        bottomAction = {
            PrimaryButton(
                text = stringResource(Res.string.form_save),
                onClick = viewModel::save,
                loading = state.saving,
                enabled = state.loaded && state.pet != null,
            )
        },
    ) {
        val pet = state.pet
        when {
            !state.loaded -> LoadingState()
            pet == null -> AlertCard { Text(stringResource(Res.string.form_no_pet), style = BestIdTheme.typography.body) }
            else -> {
                // Fotos de la cartilla: se llenan los datos mirándolas (D29). Sin fotos, se agregan en "Más detalles".
                if (state.pages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                        SectionTitle(stringResource(Res.string.form_pages_title))
                        Text(stringResource(Res.string.form_pages_hint), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                        ) {
                            state.pages.forEachIndexed { index, page ->
                                PhotoThumb(
                                    image = page.path,
                                    onClick = { sheet = PhotoSheet.Page(page, index + 1) },
                                    contentDescription = stringResource(Res.string.photo_page_description, index + 1),
                                )
                            }
                        }
                        if (state.pages.size < MAX_CARD_PAGES) AddPageButton()
                    }
                }
                if (state.cameraDenied) CameraDeniedCard(onOpenSettings = openSettings)
                if (state.photoFailed > 0) PhotoFailedCard(state.photoFailed)

                // Tipos
                Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                    SectionTitle(stringResource(Res.string.form_types_question))
                    val speciesName = stringResource(pet.species.label()).lowercase()
                    Text(
                        if (state.showAllTypes || !state.hasSuggestions) stringResource(Res.string.form_types_all_hint)
                        else stringResource(Res.string.form_types_for, speciesName),
                        style = BestIdTheme.typography.body,
                        color = BestIdTheme.colors.muted,
                    )
                    val cells = state.visibleTypes.map { TypeCell.Catalog(it.id, it.label) } +
                        state.customLabels.map { TypeCell.Custom(it) } + TypeCell.Other
                    cells.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                            row.forEach { cell ->
                                val m = Modifier.weight(1f)
                                when (cell) {
                                    is TypeCell.Catalog -> ChipToggle(
                                        cell.label,
                                        selected = cell.id in state.selectedTypeIds,
                                        onToggle = { on -> viewModel.toggleType(cell.id, on) },
                                        modifier = m,
                                    )
                                    is TypeCell.Custom -> ChipToggle(
                                        cell.label,
                                        selected = true,
                                        onToggle = { viewModel.removeCustomLabel(cell.label) },
                                        modifier = m,
                                    )
                                    TypeCell.Other -> DashedButton(
                                        stringResource(Res.string.form_type_other),
                                        onClick = viewModel::showCustomInput,
                                        modifier = m,
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (state.customInputVisible) {
                        FormField(
                            label = stringResource(Res.string.form_type_other_label),
                            value = state.customInput,
                            onValueChange = viewModel::onCustomInput,
                            placeholder = stringResource(Res.string.form_type_other_placeholder),
                        )
                        SecondaryButton(stringResource(Res.string.form_type_other_add), onClick = viewModel::addCustomLabel, icon = BestIdIcons.Add)
                    }
                    if (state.hasSuggestions) {
                        TextAction(
                            text = if (state.showAllTypes) stringResource(Res.string.form_types_show_suggested, stringResource(pet.species.label()).lowercase())
                            else stringResource(Res.string.form_types_show_all),
                            onClick = viewModel::toggleShowAll,
                        )
                    }
                    if (VaccineError.NoType in state.errors) FieldError(stringResource(Res.string.form_error_type))
                }

                // Fechas
                val applicationError = when {
                    VaccineError.NoApplicationDate in state.errors -> stringResource(Res.string.form_error_date)
                    VaccineError.ApplicationDateInFuture in state.errors -> stringResource(Res.string.form_error_future)
                    else -> null
                }
                DateField(
                    label = stringResource(Res.string.form_date),
                    value = state.applicationDate?.formatLong(),
                    onClick = { pick(DateTarget.Application, state.applicationDate) },
                    error = applicationError,
                )
                DateField(
                    label = stringResource(Res.string.form_next_date),
                    value = state.nextDate?.formatLong(),
                    onClick = { pick(DateTarget.Next, state.nextDate ?: state.applicationDate) },
                    optional = true,
                    error = if (VaccineError.NextDateNotAfterApplication in state.errors) stringResource(Res.string.form_error_next) else null,
                )

                // Más detalles
                ExpandableSection(title = stringResource(Res.string.form_more), initiallyExpanded = state.isEdit) {
                    state.stickers.forEachIndexed { index, sticker ->
                        if (state.stickers.size > 1) FieldLabelText(stringResource(Res.string.form_sticker_title, index + 1))
                        FormField(
                            label = stringResource(Res.string.form_product),
                            value = sticker.product,
                            onValueChange = { viewModel.onSticker(index, sticker.copy(product = it)) },
                            placeholder = stringResource(Res.string.form_product_placeholder),
                            optional = true,
                        )
                        FormField(
                            label = stringResource(Res.string.form_lot),
                            value = sticker.lot,
                            onValueChange = { viewModel.onSticker(index, sticker.copy(lot = it)) },
                            placeholder = stringResource(Res.string.form_lot_placeholder),
                            optional = true,
                        )
                        DateField(
                            label = stringResource(Res.string.form_manufacture),
                            value = sticker.manufactureDate?.formatLong(),
                            onClick = { pick(DateTarget.Manufacture(index), sticker.manufactureDate) },
                            optional = true,
                        )
                        DateField(
                            label = stringResource(Res.string.form_expiry),
                            value = sticker.expiryDate?.formatLong(),
                            onClick = { pick(DateTarget.Expiry(index), sticker.expiryDate) },
                            optional = true,
                            error = if (VaccineError.ExpiryNotAfterManufacture(index) in state.errors) stringResource(Res.string.form_error_expiry) else null,
                        )
                        if (index in state.expiredStickers) {
                            AlertCard { Text(stringResource(Res.string.form_warning_expired), style = BestIdTheme.typography.body) }
                        }
                        if (state.stickers.size > 1) {
                            TextAction(stringResource(Res.string.form_remove_sticker), onClick = { viewModel.removeSticker(index) }, destructive = true)
                        }
                    }
                    DashedButton(stringResource(Res.string.form_add_sticker), onClick = viewModel::addSticker)
                    if (state.pages.isEmpty()) AddPageButton()
                    val stickerPhoto = state.stickerPhoto
                    if (stickerPhoto != null) {
                        FieldLabelText(stringResource(Res.string.form_sticker_photo), optional = true)
                        PhotoThumb(
                            image = stickerPhoto.path,
                            onClick = { sheet = PhotoSheet.Sticker(stickerPhoto) },
                            contentDescription = stringResource(Res.string.photo_sticker_description),
                        )
                    } else {
                        DashedButton(
                            stringResource(Res.string.form_add_sticker_photo),
                            onClick = {
                                photoTarget = PhotoTarget.STICKER
                                sheet = PhotoSheet.AddSticker
                            },
                            icon = BestIdIcons.PhotoCamera,
                            loading = state.importing && photoTarget == PhotoTarget.STICKER,
                        )
                    }
                    FormField(
                        label = stringResource(Res.string.form_vet),
                        value = state.vetName,
                        onValueChange = viewModel::onVetName,
                        placeholder = stringResource(Res.string.form_vet_placeholder),
                        optional = true,
                    )
                    val query = state.vetName.trim().lowercase()
                    vets.filter { query.isNotEmpty() && it.name.lowercase().contains(query) && it.name != state.vetName }
                        .take(3)
                        .forEach { vet -> TextAction(vet.name, onClick = { viewModel.pickVet(vet) }) }
                    FormField(
                        label = stringResource(Res.string.form_vet_contact),
                        value = state.vetContact,
                        onValueChange = viewModel::onVetContact,
                        placeholder = stringResource(Res.string.form_vet_contact_placeholder),
                        optional = true,
                        kind = FieldKind.PHONE,
                    )
                }
            }
        }
    }

    PhotoSheets(
        sheet = sheet,
        picker = picker,
        maxPages = MAX_CARD_PAGES - state.pages.size,
        onDismiss = { sheet = null },
        onOpenPhoto = onOpenPhoto,
        onRemovePage = { viewModel.removePage(it.id) },
        onRemoveSticker = viewModel::removeStickerPhoto,
    )
}

/** Celda de la cuadrícula de tipos: del catálogo, escrito a mano o el botón "Otra". */
private sealed interface TypeCell {
    data class Catalog(val id: String, val label: String) : TypeCell
    data class Custom(val label: String) : TypeCell
    data object Other : TypeCell
}
