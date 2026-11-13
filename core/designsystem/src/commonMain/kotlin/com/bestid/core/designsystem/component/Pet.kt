package com.bestid.core.designsystem.component

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
import com.bestid.core.designsystem.resources.ds_id_card_brand
import com.bestid.core.designsystem.resources.ds_id_card_next
import com.bestid.core.designsystem.resources.ds_id_card_vaccines
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
            .clip(BestIdTheme.shapes.pill)
            .background(colors.surface, BestIdTheme.shapes.pill)
            .border(BorderStroke(BestIdTheme.sizes.border, colors.line), BestIdTheme.shapes.pill)
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
 * Carnet BestID de la pestaña Mascota (y base de la ficha en PDF): encabezado verde con la marca,
 * foto, datos y franja con número de vacunas y próxima fecha.
 */
@Composable
fun PetIdCard(
    name: String,
    photo: String?,
    species: AvatarSpecies,
    details: List<Pair<String, String>>,
    vaccineCount: String,
    nextDate: String,
    modifier: Modifier = Modifier,
) {
    val colors = BestIdTheme.colors
    val shape = BestIdTheme.shapes.cardLarge
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
                .background(colors.primary)
                .padding(horizontal = BestIdTheme.spacing.lg, vertical = BestIdTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
        ) {
            Icon(painterResource(BestIdIcons.PetsFilled), contentDescription = null, tint = colors.onPrimary)
            Text(stringResource(Res.string.ds_id_card_brand), style = BestIdTheme.typography.card, color = colors.onPrimary)
        }
        Row(
            modifier = Modifier.padding(BestIdTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
        ) {
            PetAvatar(photo, species, name, size = AvatarSize.LARGE)
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = BestIdTheme.typography.title, color = colors.ink, modifier = Modifier.semantics { heading() })
                details.forEach { (label, value) -> ListRow(label, value) }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primarySoft)
                .padding(horizontal = BestIdTheme.spacing.lg, vertical = BestIdTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
        ) {
            IdStripItem(stringResource(Res.string.ds_id_card_vaccines), vaccineCount, Modifier.weight(1f))
            IdStripItem(stringResource(Res.string.ds_id_card_next), nextDate, Modifier.weight(1f))
        }
    }
}

@Composable
private fun IdStripItem(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, style = BestIdTheme.typography.label, color = BestIdTheme.colors.primaryInk)
        Text(value, style = BestIdTheme.typography.body, color = BestIdTheme.colors.primaryInk)
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
        details = listOf(
            PreviewData.ID_SPECIES to PreviewData.SPECIES.first(),
            PreviewData.ID_SEX to PreviewData.SEX.first(),
        ),
        vaccineCount = PreviewData.ID_VACCINES,
        nextDate = PreviewData.ID_NEXT,
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
        details = listOf(PreviewData.ID_BIRTH to PreviewData.LONG_TEXT),
        vaccineCount = PreviewData.ID_VACCINES,
        nextDate = PreviewData.LONG_TEXT,
    )
}
