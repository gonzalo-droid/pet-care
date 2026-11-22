package com.bestid.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.CountdownRing
import com.bestid.core.designsystem.component.DateTile
import com.bestid.core.designsystem.component.ProgressRow
import com.bestid.core.designsystem.component.ReviewStrip
import com.bestid.core.designsystem.component.TimelineItem
import com.bestid.core.designsystem.component.AvatarSize
import com.bestid.core.designsystem.component.AvatarSpecies
import com.bestid.core.designsystem.component.Badge
import com.bestid.core.designsystem.component.BadgeTone
import com.bestid.core.designsystem.component.CelebrationBadge
import com.bestid.core.designsystem.component.ChipChoice
import com.bestid.core.designsystem.component.ChipToggle
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.DashedButton
import com.bestid.core.designsystem.component.DateField
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.ExpandableSection
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.FormField
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.ListRow
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.NotificationPreview
import com.bestid.core.designsystem.component.PermissionCard
import com.bestid.core.designsystem.component.PetAvatar
import com.bestid.core.designsystem.component.PetIdCard
import com.bestid.core.designsystem.component.PetSelector
import com.bestid.core.designsystem.component.PetSelectorItem
import com.bestid.core.designsystem.component.PhotoThumb
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.ReviewField
import com.bestid.core.designsystem.component.ReviewRecordBlock
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.SectionTitle
import com.bestid.core.designsystem.component.SegmentedControl
import com.bestid.core.designsystem.component.SelectField
import com.bestid.core.designsystem.component.SelectOption
import com.bestid.core.designsystem.component.StatCard
import com.bestid.core.designsystem.component.SwitchRow
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.component.VaccineRecordCard
import com.bestid.core.designsystem.component.WeightChart
import com.bestid.core.designsystem.component.WeightPoint
import com.bestid.core.designsystem.component.formatKg
import com.bestid.core.designsystem.component.showUndo
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_undo
import com.bestid.core.designsystem.theme.BestIdTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Catálogo de todos los componentes, interactivo, para revisarlos en un teléfono real
 * (sobre todo en iPhone, donde no hay previews). Herramienta de desarrollo: usa [PreviewData].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentCatalog(title: String, onClose: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val undoLabel = stringResource(Res.string.ds_undo)
    var name by remember { mutableStateOf("") }
    var types by remember { mutableStateOf(setOf(PreviewData.VACCINE_TYPES.first())) }
    var sex by remember { mutableIntStateOf(0) }
    var segment by remember { mutableIntStateOf(0) }
    var reminders by remember { mutableStateOf(true) }
    var included by remember { mutableStateOf(true) }
    var species by remember { mutableStateOf<String?>(null) }
    var confirmOpen by remember { mutableStateOf(false) }
    var selectedPet by remember { mutableStateOf("1") }
    val pets = listOf(
        PetSelectorItem("1", PreviewData.PET_NAME, null, AvatarSpecies.CAT),
        PetSelectorItem("2", PreviewData.PET_NAME_2, null, AvatarSpecies.DOG),
    )

    FlowScaffold(
        title = title,
        onClose = onClose,
        snackbarHostState = snackbar,
        bottomAction = { PrimaryButton(PreviewData.BUTTON, onClick = { confirmOpen = true }) },
    ) {
        SectionTitle("PetSelector · PetAvatar")
        PetSelector(pets, selectedPet, onSelect = { selectedPet = it }, onAdd = {})
        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            PetAvatar(null, AvatarSpecies.CAT, PreviewData.PET_NAME, size = AvatarSize.SMALL)
            PetAvatar(null, AvatarSpecies.DOG, PreviewData.PET_NAME_2)
            PetAvatar(null, AvatarSpecies.OTHER, PreviewData.PET_NAME, size = AvatarSize.LARGE)
        }

        SectionTitle("Botones")
        PrimaryButton(PreviewData.BUTTON, onClick = {}, icon = BestIdIcons.PhotoCamera)
        PrimaryButton(PreviewData.BUTTON, onClick = {}, loading = true)
        SecondaryButton(PreviewData.BUTTON_SECONDARY, onClick = {}, icon = BestIdIcons.Image)
        SecondaryButton(PreviewData.BUTTON_SECONDARY, onClick = {}, enabled = false)
        DashedButton(PreviewData.BUTTON_DASHED, onClick = {})
        TextAction(PreviewData.TEXT_ACTION, onClick = {})

        SectionTitle("Campos")
        FormField(
            PreviewData.FIELD_LABEL,
            name,
            { name = it },
            placeholder = PreviewData.FIELD_PLACEHOLDER,
            helper = PreviewData.FIELD_HELPER,
            error = if (name.length > 40) PreviewData.FIELD_ERROR else null,
        )
        FormField(PreviewData.FIELD_LABEL, "", {}, error = PreviewData.FIELD_ERROR)
        DateField(PreviewData.DATE_LABEL, PreviewData.DATE_VALUE, onClick = {}, allowApproximate = true)
        SelectField(
            PreviewData.SPECIES_LABEL,
            PreviewData.SPECIES.map { SelectOption(it, it) },
            species,
            { species = it },
            optional = true,
        )

        SectionTitle("Chips · Segmentado · Interruptor")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
        ) {
            PreviewData.VACCINE_TYPES.forEach { type ->
                ChipToggle(type, type in types, onToggle = { on -> types = if (on) types + type else types - type })
            }
        }
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            PreviewData.SEX.forEachIndexed { i, label -> ChipChoice(label, sex == i, onSelect = { sex = i }) }
        }
        SegmentedControl(PreviewData.HEALTH_TABS, segment, onSelect = { segment = it })
        SwitchRow(PreviewData.SWITCH_TITLE, reminders, { reminders = it }, subtitle = PreviewData.SWITCH_SUBTITLE)
        ExpandableSection(PreviewData.FIELD_HELPER) {
            FormField(PreviewData.FIELD_LABEL, PreviewData.FIELD_VALUE, {})
        }

        SectionTitle("Tarjetas")
        InfoCard { Text(PreviewData.INFO, style = BestIdTheme.typography.body) }
        AlertCard { Text(PreviewData.ALERT, style = BestIdTheme.typography.body) }
        PlainCard {
            ListRow(PreviewData.ROW_LABEL, PreviewData.ROW_VALUE)
            ListRow(PreviewData.ID_SEX, PreviewData.SEX.first())
        }
        StatCard(PreviewData.STAT_LABEL, PreviewData.STAT_VALUE, actionText = PreviewData.STAT_ACTION, onAction = {})
        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            Badge(PreviewData.BADGE_OK, BadgeTone.OK)
            Badge(PreviewData.BADGE_ALERT, BadgeTone.ALERT)
            Badge(PreviewData.BADGE_NEUTRAL, BadgeTone.NEUTRAL)
        }
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

        SectionTitle("Registros y lectura")
        VaccineRecordCard(
            PreviewData.RECORD_DATE,
            PreviewData.VACCINE_TYPES.take(3),
            onClick = {
                scope.launch { snackbar.showUndo(PreviewData.UNDO_MESSAGE, undoLabel) }
            },
            products = PreviewData.RECORD_PRODUCTS,
            vet = PreviewData.RECORD_VET,
            review = PreviewData.BADGE_ALERT,
        )
        TimelineItem(date = { DateTile("21", "DIC", "21 de diciembre") }) {
            VaccineRecordCard(null, PreviewData.VACCINE_TYPES.take(2), onClick = {}, products = PreviewData.RECORD_PRODUCTS, photos = PreviewData.RECORD_PHOTOS)
        }
        TimelineItem(date = { DateTile("?", "—", "Fecha sin leer", review = true) }) {
            VaccineRecordCard(null, PreviewData.VACCINE_TYPES.take(1), onClick = {}, review = "! Revisa")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md)) {
            CountdownRing("12", "días", 0.6f)
            CountdownRing("?", "días", null)
        }
        ReviewStrip("2 datos por revisar", "Revisar", onAction = {})
        ProgressRow("3 de 5 datos completos", 0.6f, "Completar", onClick = {})
        ReviewRecordBlock(PreviewData.REVIEW_TITLE, included, { included = it }) {
            ReviewField(PreviewData.REVIEW_REASON) {
                Text(PreviewData.REVIEW_QUESTION, style = BestIdTheme.typography.body)
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                    PreviewData.REVIEW_ANSWERS.forEach { ChipChoice(it, false, onSelect = {}) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            PhotoThumb(null, onClick = {})
            PhotoThumb(null, onClick = {})
        }

        SectionTitle("Peso")
        PlainCard {
            WeightChart(
                PreviewData.WEIGHT_POINTS.map { (m, kg) -> WeightPoint(m, kg, formatKg(kg)) },
                description = PreviewData.STAT_VALUE,
            )
        }

        SectionTitle("Estados")
        NotificationPreview(PreviewData.NOTIFICATION_TITLE, PreviewData.NOTIFICATION_BODY)
        CelebrationBadge(PreviewData.INFO)
        EmptyState(BestIdIcons.Vaccines, PreviewData.EMPTY_TITLE, PreviewData.EMPTY_BODY)
        LoadingState()
        ErrorState(PreviewData.ERROR, onRetry = {})
        PermissionCard(PreviewData.PERMISSION_TITLE, PreviewData.PERMISSION_BODY, onOpenSettings = {})
    }

    if (confirmOpen) {
        ConfirmSheet(
            title = PreviewData.CONFIRM_TITLE,
            body = PreviewData.CONFIRM_BODY,
            confirmText = PreviewData.CONFIRM_ACTION,
            destructive = true,
            onConfirm = {
                confirmOpen = false
                scope.launch { snackbar.showUndo(PreviewData.UNDO_MESSAGE, undoLabel) }
            },
            onDismiss = { confirmOpen = false },
        )
    }
}
