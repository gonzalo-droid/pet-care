package com.bestid.core.designsystem.component

import com.bestid.core.designsystem.resources.ds_loading
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_add_pet
import com.bestid.core.designsystem.resources.ds_change_pet
import com.bestid.core.designsystem.resources.ds_id_card_title
import com.bestid.core.designsystem.resources.ds_id_card_next
import com.bestid.core.designsystem.theme.isLargeFont
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.DrawableResource
import com.bestid.core.designsystem.resources.ds_pet_photo
import com.bestid.core.designsystem.resources.ds_pets_title
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Especie para el placeholder del avatar. El design system no depende del modelo de dominio:
 * cada feature traduce su especie a este valor.
 */
enum class AvatarSpecies { DOG, CAT, OTHER }

/** Tamaño del avatar. */
enum class AvatarSize { SMALL, MEDIUM, LARGE }

/**
 * Foto de la mascota o, si no hay, un placeholder por especie: el gato provisional del prototipo
 * para gatos y la huella para el resto. `[Pendiente]` ilustraciones propias (sección 20).
 */
@Composable
fun PetAvatar(
    photo: String?,
    species: AvatarSpecies,
    name: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.MEDIUM,
) {
    val colors = BestIdTheme.colors
    val dp: Dp = when (size) {
        AvatarSize.SMALL -> BestIdTheme.sizes.avatarSmall
        AvatarSize.MEDIUM -> BestIdTheme.sizes.avatarMedium
        AvatarSize.LARGE -> BestIdTheme.sizes.avatarLarge
    }
    val description = stringResource(Res.string.ds_pet_photo, name)
    val shape = BestIdTheme.shapes.pill
    Box(
        modifier = modifier
            .size(dp)
            .clip(shape)
            .background(colors.primarySoft, shape)
            .border(BorderStroke(BestIdTheme.sizes.border, colors.surface), shape)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(dp),
            )
        } else if (species == AvatarSpecies.CAT) {
            Image(painterResource(BestIdIcons.CatIllustration), contentDescription = null, modifier = Modifier.size(dp))
        } else {
            Icon(
                painterResource(BestIdIcons.PetsFilled),
                contentDescription = null,
                tint = colors.primaryInk,
                modifier = Modifier.size(dp / 2),
            )
        }
    }
}

/** Mascota en el [PetSelector]. */
data class PetSelectorItem(
    val id: String,
    val name: String,
    val photo: String?,
    val species: AvatarSpecies,
)

/**
 * Selector de mascota de la barra superior: avatar y nombre; al tocar abre una hoja
 * con la lista para cambiar o agregar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSelector(
    pets: List<PetSelectorItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BestIdTheme.colors
    val selected = pets.firstOrNull { it.id == selectedId } ?: pets.firstOrNull()
    var open by remember { mutableStateOf(false) }
    if (selected == null) {
        // Sin mascotas: el selector se convierte en la acción de agregar.
        TextAction(text = stringResource(Res.string.ds_add_pet), onClick = onAdd, icon = BestIdIcons.Add, modifier = modifier)
        return
    }
    val description = stringResource(Res.string.ds_change_pet, selected.name)

    Row(
        modifier = modifier
            .heightIn(min = BestIdTheme.sizes.secondaryButton)
            .shadow(BestIdTheme.elevation.card, BestIdTheme.shapes.pill, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(BestIdTheme.shapes.pill)
            .background(colors.surface, BestIdTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = { open = true })
            .semantics { contentDescription = description }
            .padding(start = BestIdTheme.spacing.xxs, end = BestIdTheme.spacing.sm, top = BestIdTheme.spacing.xxs, bottom = BestIdTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        PetAvatar(selected.photo, selected.species, selected.name, size = AvatarSize.SMALL)
        Text(
            selected.name,
            style = BestIdTheme.typography.card,
            color = colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            // Sin fill: el chip sigue al ancho del nombre, pero un nombre largo no empuja la flecha fuera.
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(painterResource(BestIdIcons.ExpandMore), contentDescription = null, tint = colors.muted)
    }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.bg,
            shape = BestIdTheme.shapes.sheet,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
            ) {
                Text(
                    stringResource(Res.string.ds_pets_title),
                    style = BestIdTheme.typography.subtitle,
                    modifier = Modifier.semantics { heading() },
                )
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                    pets.forEach { pet ->
                        val isSelected = pet.id == selected.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = BestIdTheme.sizes.minTouch)
                                .clip(BestIdTheme.shapes.card)
                                .background(if (isSelected) colors.primarySoft else colors.surface, BestIdTheme.shapes.card)
                                .selectable(
                                    selected = isSelected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        open = false
                                        onSelect(pet.id)
                                    },
                                )
                                .padding(BestIdTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
                        ) {
                            PetAvatar(pet.photo, pet.species, pet.name, size = AvatarSize.SMALL)
                            Text(pet.name, style = BestIdTheme.typography.body, modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(painterResource(BestIdIcons.CheckCircle), contentDescription = null, tint = colors.primary)
                            }
                        }
                    }
                }
                DashedButton(
                    text = stringResource(Res.string.ds_add_pet),
                    onClick = {
                        open = false
                        onAdd()
                    },
                )
            }
        }
    }
}

/**
 * Foto de la mascota en su formulario: círculo punteado con cámara si no hay foto, o la foto.
 * [actionLabel] es "Agregar foto" o "Cambiar foto"; al tocar se eligen la cámara o la galería.
 */
@Composable
fun PetPhotoPicker(
    photo: String?,
    species: AvatarSpecies,
    name: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val colors = BestIdTheme.colors
    val strokeWidth = BestIdTheme.sizes.border
    val outline = colors.outlineStrong
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !loading, role = Role.Button, onClickLabel = actionLabel, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        if (photo != null) {
            PetAvatar(photo, species, name, size = AvatarSize.LARGE)
        } else {
            Box(
                modifier = Modifier
                    .size(BestIdTheme.sizes.avatarLarge)
                    .clip(BestIdTheme.shapes.pill)
                    .background(colors.surface)
                    .drawWithContent {
                        drawContent()
                        drawCircle(
                            color = outline,
                            style = Stroke(strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
                            radius = size.minDimension / 2 - strokeWidth.toPx() / 2,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(BestIdIcons.PhotoCamera), contentDescription = null, tint = colors.primary)
            }
        }
        Text(
            if (loading) stringResource(Res.string.ds_loading) else actionLabel,
            style = BestIdTheme.typography.bodyStrong,
            color = colors.primary,
        )
    }
}

/**
 * Carnet BestID de la pestaña Mascota: encabezado verde con trama de puntos y la marca, foto que lo
 * monta con aro blanco, nombre y [subtitle] ("Gata · Hembra"), [details] en dos columnas (una con
 * letra grande), [completion] opcional (por ejemplo, la fila "Faltan…"), separador perforado y pie
 * con [vaccineCount] ("2 vacunas") y la próxima fecha.
 */
@Composable
fun PetIdCard(
    name: String,
    photo: String?,
    species: AvatarSpecies,
    subtitle: String,
    details: List<Pair<String, String>>,
    vaccineCount: String,
    nextDate: String,
    completion: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = BestIdTheme.colors
    val sizes = BestIdTheme.sizes
    val spacing = BestIdTheme.spacing
    val shape = BestIdTheme.shapes.cardLarge
    val largeFont = isLargeFont()
    val dot = colors.onPrimary.copy(alpha = 0.08f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(BestIdTheme.elevation.card, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(shape)
            .background(colors.surface, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = sizes.idCardHeader)
                .background(colors.primary)
                .drawBehind {
                    val step = sizes.idCardDots.toPx()
                    // Trama del prototipo: un círculo de 7/44 del paso, centrado a 20/44 de cada celda.
                    var y = step * 20 / 44
                    while (y < size.height + step) {
                        var x = step * 20 / 44
                        while (x < size.width + step) {
                            drawCircle(dot, radius = step * 7 / 44, center = Offset(x, y))
                            x += step
                        }
                        y += step
                    }
                }
                .padding(horizontal = spacing.lg, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            BrandLogo(style = BestIdTheme.typography.subtitle, onPrimary = true)
            Text(
                stringResource(Res.string.ds_id_card_title),
                style = BestIdTheme.typography.eyebrow,
                color = colors.onPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(top = spacing.xxs),
            )
        }
        Column(
            modifier = Modifier.padding(start = spacing.lg, end = spacing.lg, bottom = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            val avatar: @Composable () -> Unit = {
                Box(Modifier.background(colors.surface, BestIdTheme.shapes.pill).padding(sizes.idCardRing)) {
                    PetAvatar(photo, species, name, size = AvatarSize.LARGE)
                }
            }
            val identity: @Composable (Modifier) -> Unit = { m ->
                Column(m.padding(bottom = spacing.xxs)) {
                    Text(name, style = BestIdTheme.typography.title, color = colors.ink, modifier = Modifier.semantics { heading() })
                    Text(subtitle, style = BestIdTheme.typography.body, color = colors.muted)
                }
            }
            if (largeFont) {
                Box(Modifier.liftBy(sizes.idCardAvatarLift)) { avatar() }
                identity(Modifier)
            } else {
                Row(
                    modifier = Modifier.liftBy(sizes.idCardAvatarLift),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    avatar()
                    identity(Modifier.weight(1f))
                }
            }
            val columns = if (largeFont) 1 else 2
            details.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    row.forEach { (label, value) ->
                        Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
                            Eyebrow(label)
                            Text(value, style = BestIdTheme.typography.bodyStrong, color = colors.ink)
                        }
                    }
                    repeat(columns - row.size) { Box(Modifier.weight(1f)) }
                }
            }
            completion?.invoke()
        }
        PerforatedDivider()
        val next = stringResource(Res.string.ds_id_card_next, nextDate)
        val footer: @Composable (Modifier, Modifier) -> Unit = { first, second ->
            IdFooterItem(BestIdIcons.CheckCircle, vaccineCount, first)
            IdFooterItem(BestIdIcons.Calendar, next, second)
        }
        if (largeFont) {
            Column(Modifier.padding(horizontal = spacing.lg, vertical = spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                footer(Modifier, Modifier)
            }
        } else {
            Row(Modifier.padding(horizontal = spacing.lg, vertical = spacing.md), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                footer(Modifier.weight(1f), Modifier.weight(1f))
            }
        }
    }
}

/** Sube el contenido [lift] sin dejar el hueco abajo (el avatar monta el encabezado). */
private fun Modifier.liftBy(lift: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val px = lift.roundToPx()
    layout(placeable.width, (placeable.height - px).coerceAtLeast(0)) { placeable.place(0, -px) }
}

/** Línea punteada de borde a borde con dos muescas del color del fondo, como un boleto. */
@Composable
private fun PerforatedDivider() {
    val colors = BestIdTheme.colors
    val stroke = BestIdTheme.sizes.border
    val dash = BestIdTheme.spacing.xs
    val gap = BestIdTheme.spacing.xxs
    Box(
        Modifier
            .fillMaxWidth()
            .height(BestIdTheme.sizes.idCardNotch)
            .drawBehind {
                val mid = size.height / 2
                drawLine(
                    colors.line,
                    Offset(0f, mid),
                    Offset(size.width, mid),
                    strokeWidth = stroke.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx())),
                )
                drawCircle(colors.bg, radius = mid, center = Offset(0f, mid))
                drawCircle(colors.bg, radius = mid, center = Offset(size.width, mid))
            },
    )
}

@Composable
private fun IdFooterItem(icon: DrawableResource, text: String, modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = BestIdTheme.colors.primary, modifier = Modifier.size(BestIdTheme.sizes.iconSmall))
        Text(text, style = BestIdTheme.typography.bodyStrong, color = BestIdTheme.colors.ink)
    }
}

private val previewPets = listOf(
    PetSelectorItem("1", PreviewData.PET_NAME, null, AvatarSpecies.CAT),
    PetSelectorItem("2", PreviewData.PET_NAME_2, null, AvatarSpecies.DOG),
)

@Preview
@Composable
private fun PetPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        PetAvatar(null, AvatarSpecies.CAT, PreviewData.PET_NAME, size = AvatarSize.SMALL)
        PetAvatar(null, AvatarSpecies.DOG, PreviewData.PET_NAME_2, size = AvatarSize.MEDIUM)
        PetAvatar(null, AvatarSpecies.OTHER, PreviewData.PET_NAME, size = AvatarSize.LARGE)
    }
    PetSelector(previewPets, selectedId = "1", onSelect = {}, onAdd = {})
    PetSelector(emptyList(), selectedId = null, onSelect = {}, onAdd = {})
    PetIdCard(
        name = PreviewData.PET_NAME,
        photo = null,
        species = AvatarSpecies.CAT,
        subtitle = PreviewData.ID_SUBTITLE,
        details = listOf(PreviewData.ID_OWNER to PreviewData.ID_OWNER_NAME),
        vaccineCount = PreviewData.ID_VACCINES,
        nextDate = PreviewData.ID_NEXT,
        completion = { ProgressRow(PreviewData.ID_MISSING, 0.25f, PreviewData.ID_COMPLETE, onClick = {}) },
    )
}

@Preview(fontScale = 2f)
@Composable
private fun PetLongTextPreview() = PreviewSurface {
    PetSelector(
        listOf(PetSelectorItem("1", PreviewData.LONG_TEXT, null, AvatarSpecies.CAT)),
        selectedId = "1",
        onSelect = {},
        onAdd = {},
    )
    PetIdCard(
        name = PreviewData.LONG_TEXT,
        photo = null,
        species = AvatarSpecies.DOG,
        subtitle = PreviewData.LONG_TEXT,
        details = listOf(PreviewData.ID_BIRTH to PreviewData.LONG_TEXT, PreviewData.ID_OWNER to PreviewData.ID_OWNER_NAME),
        completion = { ProgressRow(PreviewData.ID_MISSING, 0.5f, PreviewData.ID_COMPLETE, onClick = {}) },
        vaccineCount = PreviewData.ID_VACCINES,
        nextDate = PreviewData.LONG_TEXT,
    )
}
