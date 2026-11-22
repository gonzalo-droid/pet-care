package com.bestid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_include
import com.bestid.core.designsystem.resources.ds_review_reason
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Registro de vacuna en la Cartilla: fecha, productos, chips de tipos y veterinaria.
 * [review] muestra un badge "Revisa" cuando falta confirmar algún dato.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaccineRecordCard(
    date: String?,
    types: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    products: String? = null,
    vet: String? = null,
    review: String? = null,
    /** "2 fotos de la cartilla", con el ícono de imagen. */
    photos: String? = null,
    /** Qué abre al tocar ("Ver vacuna del 21 dic 2025"): necesario si no se muestra [date]. */
    onClickLabel: String? = null,
) {
    val colors = BestIdTheme.colors
    // Sin fecha propia (dentro de un timeline) el chevron va centrado a la derecha de toda la tarjeta.
    PlainCard(modifier = modifier, onClick = onClick, onClickLabel = onClickLabel, showChevron = date == null) {
        if (date != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(date, style = BestIdTheme.typography.subtitle, color = colors.ink, modifier = Modifier.weight(1f))
                Icon(painterResource(BestIdIcons.ChevronRight), contentDescription = null, tint = colors.muted)
            }
        }
        if (types.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
                verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
            ) { types.forEach { Badge(it, BadgeTone.OK) } }
        }
        if (products != null) Text(products, style = BestIdTheme.typography.support, color = colors.muted)
        if (photos != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs)) {
                Icon(painterResource(BestIdIcons.Image), contentDescription = null, tint = colors.muted, modifier = Modifier.size(BestIdTheme.sizes.iconSmall))
                Text(photos, style = BestIdTheme.typography.support, color = colors.muted)
            }
        }
        if (vet != null) Text(vet, style = BestIdTheme.typography.support, color = colors.muted)
        if (review != null) Badge(review, BadgeTone.ALERT)
    }
}

/**
 * Dato dudoso de la lectura: fondo durazno, ícono y la palabra "Revisa" con el motivo.
 * El contenido es una pregunta (ChipChoice) o un campo para completar.
 */
@Composable
fun ReviewField(
    reason: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BestIdTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.alert, BestIdTheme.shapes.field)
            .padding(horizontal = BestIdTheme.spacing.md, vertical = BestIdTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(painterResource(BestIdIcons.Error), contentDescription = null, tint = colors.alertInk)
            Text(
                text = stringResource(Res.string.ds_review_reason, reason),
                style = BestIdTheme.typography.bodyStrong,
                color = colors.alertInk,
                modifier = Modifier.weight(1f),
            )
        }
        content()
    }
}

/**
 * Bloque de un registro detectado en "Revisar lectura" (O4), con el interruptor "Incluir".
 * Los campos van en [content]; los dudosos se envuelven en [ReviewField].
 */
@Composable
fun ReviewRecordBlock(
    title: String,
    included: Boolean,
    onToggleIncluded: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlainCard(modifier = modifier) {
        Text(
            title,
            style = BestIdTheme.typography.card,
            color = BestIdTheme.colors.ink,
            modifier = Modifier.semantics { heading() },
        )
        SwitchRow(title = stringResource(Res.string.ds_include), checked = included, onCheckedChange = onToggleIncluded)
        if (included) content()
    }
}

@Preview
@Composable
private fun RecordsPreview() = PreviewSurface {
    VaccineRecordCard(
        date = PreviewData.RECORD_DATE,
        types = PreviewData.VACCINE_TYPES.take(3),
        onClick = {},
        products = PreviewData.RECORD_PRODUCTS,
        vet = PreviewData.RECORD_VET,
    )
    VaccineRecordCard(
        date = null,
        types = PreviewData.VACCINE_TYPES.take(2),
        onClick = {},
        products = PreviewData.RECORD_PRODUCTS,
        photos = PreviewData.RECORD_PHOTOS,
    )
    ReviewRecordBlock(PreviewData.REVIEW_TITLE, included = true, onToggleIncluded = {}) {
        DateField(PreviewData.DATE_LABEL, PreviewData.DATE_VALUE, onClick = {})
        ReviewField(PreviewData.REVIEW_REASON) {
            Text(PreviewData.REVIEW_QUESTION, style = BestIdTheme.typography.body)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                PreviewData.REVIEW_ANSWERS.forEach { ChipChoice(it, selected = false, onSelect = {}) }
            }
        }
    }
}

@Preview
@Composable
private fun RecordsErrorPreview() = PreviewSurface {
    VaccineRecordCard(
        date = PreviewData.RECORD_DATE,
        types = PreviewData.VACCINE_TYPES.take(1),
        onClick = {},
        review = PreviewData.BADGE_ALERT,
    )
    ReviewRecordBlock(PreviewData.REVIEW_TITLE, included = false, onToggleIncluded = {}) {}
}

@Preview(fontScale = 2f)
@Composable
private fun RecordsLongTextPreview() = PreviewSurface {
    VaccineRecordCard(
        date = PreviewData.LONG_TEXT,
        types = PreviewData.VACCINE_TYPES,
        onClick = {},
        products = PreviewData.LONG_TEXT,
        vet = PreviewData.LONG_TEXT,
        photos = PreviewData.LONG_TEXT,
    )
    ReviewField(PreviewData.LONG_TEXT) {
        FormField(PreviewData.DATE_LABEL, "", {}, placeholder = PreviewData.DATE_VALUE)
    }
}
