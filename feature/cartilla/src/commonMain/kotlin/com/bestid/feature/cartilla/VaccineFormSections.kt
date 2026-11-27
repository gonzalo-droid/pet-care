package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.ChipToggle
import com.bestid.core.designsystem.component.DashedButton
import com.bestid.core.designsystem.component.DateField
import com.bestid.core.designsystem.component.FieldError
import com.bestid.core.designsystem.component.FieldLabelText
import com.bestid.core.designsystem.component.FormField
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.SectionTitle
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.validation.VaccineError
import com.bestid.core.ui.formatLong
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.form_error_expiry
import com.bestid.feature.cartilla.resources.form_error_type
import com.bestid.feature.cartilla.resources.form_expiry
import com.bestid.feature.cartilla.resources.form_lot
import com.bestid.feature.cartilla.resources.form_lot_placeholder
import com.bestid.feature.cartilla.resources.form_manufacture
import com.bestid.feature.cartilla.resources.form_product
import com.bestid.feature.cartilla.resources.form_product_placeholder
import com.bestid.feature.cartilla.resources.form_remove_sticker
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
import com.bestid.feature.cartilla.resources.form_warning_expired
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

// Secciones del formulario de vacuna (C3), separadas de VaccineFormScreen.kt para que no pase de ~300 líneas (COD-05).

/** Tipos de vacuna en cuadrícula de 2: del catálogo filtrado por especie, escritos a mano y "Otra". */
@Composable
internal fun VaccineTypesSection(state: VaccineFormState, speciesName: String, viewModel: VaccineFormViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        SectionTitle(stringResource(Res.string.form_types_question))
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
                text = if (state.showAllTypes) stringResource(Res.string.form_types_show_suggested, speciesName)
                else stringResource(Res.string.form_types_show_all),
                onClick = viewModel::toggleShowAll,
            )
        }
        if (VaccineError.NoType in state.errors) FieldError(stringResource(Res.string.form_error_type))
    }
}

/** Producto, lote y fechas de cada sticker (en "Más detalles"). */
@Composable
internal fun StickerFields(state: VaccineFormState, viewModel: VaccineFormViewModel, onPickDate: (DateTarget, LocalDate?) -> Unit) {
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
            onClick = { onPickDate(DateTarget.Manufacture(index), sticker.manufactureDate) },
            optional = true,
        )
        DateField(
            label = stringResource(Res.string.form_expiry),
            value = sticker.expiryDate?.formatLong(),
            onClick = { onPickDate(DateTarget.Expiry(index), sticker.expiryDate) },
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
}

/** Celda de la cuadrícula de tipos: del catálogo, escrito a mano o el botón "Otra". */
private sealed interface TypeCell {
    data class Catalog(val id: String, val label: String) : TypeCell
    data class Custom(val label: String) : TypeCell
    data object Other : TypeCell
}
