package com.bestid.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_collapsed_state
import com.bestid.core.designsystem.resources.ds_expanded_state
import com.bestid.core.designsystem.resources.ds_not_selected_state
import com.bestid.core.designsystem.resources.ds_selected_state
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Opción de multiselección (tipos de vacuna): fila con casilla visible. Seleccionada lleva
 * casilla marcada, borde primary y fondo primarySoft: nunca solo color.
 */
@Composable
fun ChipToggle(
    label: String,
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OptionRow(
        label = label,
        selected = selected,
        enabled = enabled,
        modifier = modifier.toggleable(
            value = selected,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onToggle,
        ),
    ) {
        Checkbox(checked = selected, onCheckedChange = null, enabled = enabled, colors = optionCheckboxColors())
    }
}

/** Opción de selección única (sexo, respuestas de "Revisar lectura"). Se agrupa con `Modifier.selectableGroup()`. */
@Composable
fun ChipChoice(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OptionRow(
        label = label,
        selected = selected,
        enabled = enabled,
        modifier = modifier.selectable(
            selected = selected,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onSelect,
        ),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = BestIdTheme.colors.primary,
                unselectedColor = BestIdTheme.colors.outlineStrong,
                disabledSelectedColor = BestIdTheme.colors.muted,
                disabledUnselectedColor = BestIdTheme.colors.line,
            ),
        )
    }
}

@Composable
private fun optionCheckboxColors() = CheckboxDefaults.colors(
    checkedColor = BestIdTheme.colors.primary,
    uncheckedColor = BestIdTheme.colors.outlineStrong,
    checkmarkColor = BestIdTheme.colors.onPrimary,
    disabledCheckedColor = BestIdTheme.colors.muted,
    disabledUncheckedColor = BestIdTheme.colors.line,
)

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    control: @Composable () -> Unit,
) {
    val colors = BestIdTheme.colors
    val shape = BestIdTheme.shapes.field
    val container = when {
        !enabled -> colors.track
        selected -> colors.primarySoft
        else -> colors.surface
    }
    val border = when {
        !enabled -> colors.line
        selected -> colors.primary
        else -> colors.outlineStrong
    }
    Row(
        modifier = Modifier
            .heightIn(min = BestIdTheme.sizes.secondaryButton)
            .clip(shape)
            .background(container, shape)
            .border(BorderStroke(BestIdTheme.sizes.border, border), shape)
            .then(modifier)
            .padding(start = BestIdTheme.spacing.xxs, end = BestIdTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        control()
        Text(label, style = BestIdTheme.typography.bodyStrong, color = if (enabled) colors.ink else colors.muted)
    }
}

/** Control segmentado (Salud: Cuidados | Peso). */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BestIdTheme.colors
    val shape = BestIdTheme.shapes.pill
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.track, shape)
            .padding(BestIdTheme.spacing.xxs)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Text(
                text = option,
                style = if (selected) BestIdTheme.typography.bodyStrong else BestIdTheme.typography.body,
                // Texto `ink` en ambos estados: muted sobre track quedaba en 4,54:1.
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BestIdTheme.sizes.minTouch)
                    .clip(shape)
                    .background(if (selected) colors.surface else colors.track, shape)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                    .padding(vertical = BestIdTheme.spacing.sm),
            )
        }
    }
}

/** Fila con interruptor (activar avisos de un cuidado). Toda la fila es tocable. */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val colors = BestIdTheme.colors
    val state = stringResource(if (checked) Res.string.ds_selected_state else Res.string.ds_not_selected_state)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BestIdTheme.sizes.minTouch)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { stateDescription = state }
            .padding(vertical = BestIdTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = BestIdTheme.typography.body, color = if (enabled) colors.ink else colors.muted)
            if (subtitle != null) {
                Text(subtitle, style = BestIdTheme.typography.support, color = colors.muted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.outlineStrong,
                uncheckedTrackColor = colors.track,
                uncheckedBorderColor = colors.outlineStrong,
            ),
        )
    }
}

/** Sección plegable ("Más detalles"). */
@Composable
fun ExpandableSection(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val colors = BestIdTheme.colors
    val state = stringResource(if (expanded) Res.string.ds_expanded_state else Res.string.ds_collapsed_state)
    val shape = BestIdTheme.shapes.field
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface, shape)
            .border(BorderStroke(BestIdTheme.sizes.border, colors.line), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BestIdTheme.sizes.field)
                .toggleable(value = expanded, role = Role.Button, onValueChange = { expanded = it })
                .semantics { stateDescription = state }
                .padding(horizontal = BestIdTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = BestIdTheme.typography.bodyStrong, color = colors.ink, modifier = Modifier.weight(1f))
            Icon(
                painterResource(if (expanded) BestIdIcons.ExpandLess else BestIdIcons.ExpandMore),
                contentDescription = null,
                tint = colors.ink,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = BestIdTheme.spacing.md, end = BestIdTheme.spacing.md, bottom = BestIdTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun ControlsPreview() = PreviewSurface {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        PreviewData.VACCINE_TYPES.forEachIndexed { i, type -> ChipToggle(type, selected = i < 2, onToggle = {}) }
    }
    Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        PreviewData.SEX.forEachIndexed { i, sex -> ChipChoice(sex, selected = i == 0, onSelect = {}) }
    }
    SegmentedControl(PreviewData.HEALTH_TABS, selectedIndex = 0, onSelect = {})
    SwitchRow(PreviewData.SWITCH_TITLE, checked = true, onCheckedChange = {}, subtitle = PreviewData.SWITCH_SUBTITLE)
    ExpandableSection(PreviewData.FIELD_HELPER, initiallyExpanded = true) {
        FormField(PreviewData.FIELD_LABEL, PreviewData.FIELD_VALUE, {})
    }
}

@Preview
@Composable
private fun ControlsDisabledPreview() = PreviewSurface {
    ChipToggle(PreviewData.VACCINE_TYPES.first(), selected = true, onToggle = {}, enabled = false)
    ChipChoice(PreviewData.SEX.first(), selected = false, onSelect = {}, enabled = false)
    SwitchRow(PreviewData.SWITCH_TITLE, checked = false, onCheckedChange = {}, enabled = false)
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(fontScale = 2f)
@Composable
private fun ControlsLongTextPreview() = PreviewSurface {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        PreviewData.VACCINE_TYPES.forEach { ChipToggle(it, selected = true, onToggle = {}) }
    }
    SegmentedControl(PreviewData.HEALTH_TABS, selectedIndex = 1, onSelect = {})
    SwitchRow(PreviewData.LONG_TEXT, checked = true, onCheckedChange = {}, subtitle = PreviewData.LONG_TEXT)
}
