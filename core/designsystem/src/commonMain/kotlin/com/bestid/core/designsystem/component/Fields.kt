package com.bestid.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_approximate_date
import com.bestid.core.designsystem.resources.ds_choose_date
import com.bestid.core.designsystem.resources.ds_choose_option
import com.bestid.core.designsystem.resources.ds_optional
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Tipo de teclado de un [FormField]. */
enum class FieldKind { TEXT, NUMBER, DECIMAL, PHONE, MULTILINE }

/**
 * Campo de formulario con etiqueta arriba, ayuda y error. El error se muestra con ícono y texto
 * (nunca solo con color) y se anuncia al lector de pantalla.
 */
@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helper: String? = null,
    error: String? = null,
    optional: Boolean = false,
    kind: FieldKind = FieldKind.TEXT,
    enabled: Boolean = true,
) {
    val colors = BestIdTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        FieldLabel(label = label, optional = optional)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = error != null,
            singleLine = kind != FieldKind.MULTILINE,
            minLines = if (kind == FieldKind.MULTILINE) 3 else 1,
            textStyle = BestIdTheme.typography.input,
            placeholder = placeholder?.let { { Text(it, style = BestIdTheme.typography.input) } },
            keyboardOptions = KeyboardOptions(keyboardType = kind.keyboardType()),
            shape = BestIdTheme.shapes.field,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.ink,
                unfocusedTextColor = colors.ink,
                disabledTextColor = colors.muted,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                disabledContainerColor = colors.track,
                errorContainerColor = colors.surface,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outlineStrong,
                disabledBorderColor = colors.line,
                errorBorderColor = colors.error,
                cursorColor = colors.primary,
                focusedPlaceholderColor = colors.muted,
                unfocusedPlaceholderColor = colors.muted,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BestIdTheme.sizes.field)
                .semantics {
                    contentDescription = label
                    if (error != null) error(error)
                },
        )
        FieldSupport(helper = helper, error = error)
    }
}

/**
 * Campo de fecha de solo lectura: al tocarlo, la pantalla abre el selector nativo de fecha.
 * El valor llega ya formateado (dd/MM/aaaa).
 */
@Composable
fun DateField(
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    helper: String? = null,
    error: String? = null,
    optional: Boolean = false,
    enabled: Boolean = true,
    allowApproximate: Boolean = false,
    approximate: Boolean = false,
    onApproximateChange: (Boolean) -> Unit = {},
) {
    val chooseDate = stringResource(Res.string.ds_choose_date)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        FieldLabel(label = label, optional = optional)
        ReadOnlyBox(
            text = value ?: chooseDate,
            isPlaceholder = value == null,
            trailingIcon = BestIdIcons.Calendar,
            onClick = onClick,
            enabled = enabled,
            error = error,
            accessibilityLabel = "$label: ${value ?: chooseDate}",
        )
        if (allowApproximate) {
            SwitchRow(
                title = stringResource(Res.string.ds_approximate_date),
                checked = approximate,
                onCheckedChange = onApproximateChange,
                enabled = enabled,
            )
        }
        FieldSupport(helper = helper, error = error)
    }
}

/** Opción de un [SelectField]. */
data class SelectOption<T>(val value: T, val label: String)

/** Selección de una opción de una lista corta (especie, tipo de cuidado). */
@Composable
fun <T> SelectField(
    label: String,
    options: List<SelectOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    helper: String? = null,
    error: String? = null,
    optional: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == selected }?.label
    val placeholder = stringResource(Res.string.ds_choose_option)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        FieldLabel(label = label, optional = optional)
        Box {
            ReadOnlyBox(
                text = selectedLabel ?: placeholder,
                isPlaceholder = selectedLabel == null,
                trailingIcon = if (expanded) BestIdIcons.ExpandLess else BestIdIcons.ExpandMore,
                onClick = { expanded = true },
                enabled = enabled,
                error = error,
                accessibilityLabel = "$label: ${selectedLabel ?: placeholder}",
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = BestIdTheme.colors.surface,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label, style = BestIdTheme.typography.body) },
                        trailingIcon = if (option.value == selected) {
                            { Icon(painterResource(BestIdIcons.Check), contentDescription = null) }
                        } else null,
                        onClick = {
                            expanded = false
                            onSelect(option.value)
                        },
                        modifier = Modifier.heightIn(min = BestIdTheme.sizes.minTouch),
                    )
                }
            }
        }
        FieldSupport(helper = helper, error = error)
    }
}

@Composable
internal fun FieldLabel(label: String, optional: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        Text(label, style = BestIdTheme.typography.bodyStrong, color = BestIdTheme.colors.ink)
        if (optional) {
            Text(
                stringResource(Res.string.ds_optional),
                style = BestIdTheme.typography.body,
                color = BestIdTheme.colors.muted,
            )
        }
    }
}

@Composable
internal fun FieldSupport(helper: String?, error: String?) {
    val colors = BestIdTheme.colors
    when {
        error != null -> Row(
            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painterResource(BestIdIcons.Error),
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(BestIdTheme.sizes.iconSmall),
            )
            Text(error, style = BestIdTheme.typography.support, color = colors.error)
        }
        helper != null -> Text(helper, style = BestIdTheme.typography.support, color = colors.muted)
    }
}

@Composable
private fun ReadOnlyBox(
    text: String,
    isPlaceholder: Boolean,
    trailingIcon: org.jetbrains.compose.resources.DrawableResource,
    onClick: () -> Unit,
    enabled: Boolean,
    error: String?,
    accessibilityLabel: String,
) {
    val colors = BestIdTheme.colors
    val borderColor = when {
        error != null -> colors.error
        !enabled -> colors.line
        else -> colors.outlineStrong
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = BestIdTheme.sizes.field)
            .background(if (enabled) colors.surface else colors.track, BestIdTheme.shapes.field)
            .border(BorderStroke(BestIdTheme.sizes.border, borderColor), BestIdTheme.shapes.field)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = accessibilityLabel
                if (error != null) error(error)
            }
            .padding(horizontal = BestIdTheme.spacing.md, vertical = BestIdTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        Text(
            text = text,
            style = BestIdTheme.typography.input,
            color = if (isPlaceholder || !enabled) colors.muted else colors.ink,
            modifier = Modifier.weight(1f),
        )
        Icon(painterResource(trailingIcon), contentDescription = null, tint = colors.muted)
    }
}

private fun FieldKind.keyboardType(): KeyboardType = when (this) {
    FieldKind.TEXT, FieldKind.MULTILINE -> KeyboardType.Text
    FieldKind.NUMBER -> KeyboardType.Number
    FieldKind.DECIMAL -> KeyboardType.Decimal
    FieldKind.PHONE -> KeyboardType.Phone
}

@Preview
@Composable
private fun FieldsPreview() = PreviewSurface {
    FormField(PreviewData.FIELD_LABEL, PreviewData.FIELD_VALUE, {}, helper = PreviewData.FIELD_HELPER)
    FormField(PreviewData.FIELD_LABEL, "", {}, placeholder = PreviewData.FIELD_PLACEHOLDER, optional = true)
    DateField(PreviewData.DATE_LABEL, PreviewData.DATE_VALUE, onClick = {})
    SelectField(
        PreviewData.SPECIES_LABEL,
        PreviewData.SPECIES.map { SelectOption(it, it) },
        selected = PreviewData.SPECIES.first(),
        onSelect = {},
    )
}

@Preview
@Composable
private fun FieldsDisabledPreview() = PreviewSurface {
    FormField(PreviewData.FIELD_LABEL, PreviewData.FIELD_VALUE, {}, enabled = false)
    DateField(PreviewData.DATE_LABEL, null, onClick = {}, enabled = false)
    SelectField(PreviewData.SPECIES_LABEL, emptyList<SelectOption<String>>(), null, {}, enabled = false)
}

@Preview
@Composable
private fun FieldsErrorPreview() = PreviewSurface {
    FormField(PreviewData.FIELD_LABEL, "", {}, error = PreviewData.FIELD_ERROR)
    DateField(PreviewData.DATE_LABEL, null, onClick = {}, error = PreviewData.FIELD_ERROR)
    SelectField(PreviewData.SPECIES_LABEL, emptyList<SelectOption<String>>(), null, {}, error = PreviewData.FIELD_ERROR)
}

@Preview(fontScale = 2f)
@Composable
private fun FieldsLongTextPreview() = PreviewSurface {
    FormField(PreviewData.LONG_TEXT, PreviewData.LONG_TEXT, {}, helper = PreviewData.LONG_TEXT, kind = FieldKind.MULTILINE)
    DateField(PreviewData.LONG_TEXT, PreviewData.DATE_VALUE, onClick = {}, allowApproximate = true)
}
