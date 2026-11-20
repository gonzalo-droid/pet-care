package com.bestid.feature.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.ChipChoice
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.DateField
import com.bestid.core.designsystem.component.ExpandableSection
import com.bestid.core.designsystem.component.FieldError
import com.bestid.core.designsystem.component.FieldLabelText
import com.bestid.core.designsystem.component.FieldKind
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.FormField
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.OptionsSheet
import com.bestid.core.designsystem.component.PetPhotoPicker
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SheetOption
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.DeleteFailedCard
import com.bestid.core.ui.LoadFailedState
import com.bestid.core.ui.SaveFailedCard
import com.bestid.core.domain.usecase.RestorePet
import com.bestid.core.domain.validation.PetError
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.platform.date.rememberDatePickerLauncher
import com.bestid.core.platform.image.rememberImagePickerLauncher
import com.bestid.core.platform.image.rememberOpenAppSettings
import com.bestid.core.ui.CameraDeniedCard
import com.bestid.core.ui.PhotoFailedCard
import com.bestid.core.ui.avatar
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.photo_gallery
import com.bestid.core.ui.resources.photo_remove
import com.bestid.core.ui.resources.photo_take
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.label
import com.bestid.feature.pet.resources.Res
import com.bestid.feature.pet.resources.pet_close
import com.bestid.feature.pet.resources.pet_delete_body
import com.bestid.feature.pet.resources.pet_delete_confirm
import com.bestid.feature.pet.resources.pet_delete_title
import com.bestid.feature.pet.resources.pet_deleted
import com.bestid.feature.pet.resources.pet_error_name_empty
import com.bestid.feature.pet.resources.pet_error_name_long
import com.bestid.feature.pet.resources.pet_error_species
import com.bestid.feature.pet.resources.pet_form_address
import com.bestid.feature.pet.resources.pet_form_address_helper
import com.bestid.feature.pet.resources.pet_form_birth
import com.bestid.feature.pet.resources.pet_form_breed
import com.bestid.feature.pet.resources.pet_form_breed_placeholder
import com.bestid.feature.pet.resources.pet_form_delete
import com.bestid.feature.pet.resources.pet_form_edit_title
import com.bestid.feature.pet.resources.pet_form_marks
import com.bestid.feature.pet.resources.pet_form_marks_placeholder
import com.bestid.feature.pet.resources.pet_form_more
import com.bestid.feature.pet.resources.pet_form_name
import com.bestid.feature.pet.resources.pet_form_name_placeholder
import com.bestid.feature.pet.resources.pet_form_new_title
import com.bestid.feature.pet.resources.pet_form_owner
import com.bestid.feature.pet.resources.pet_form_save
import com.bestid.feature.pet.resources.pet_form_sex
import com.bestid.feature.pet.resources.pet_form_species
import com.bestid.feature.pet.resources.pet_photo_add
import com.bestid.feature.pet.resources.pet_photo_change
import com.bestid.feature.pet.resources.pet_photo_sheet
import com.bestid.feature.pet.resources.pet_undo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Orden de las especies en el formulario: primero las del catálogo de vacunas. */
private val speciesOptions = listOf(Species.CAT, Species.DOG, Species.BIRD, Species.SMALL_MAMMAL, Species.REPTILE, Species.OTHER)

/** Agregar o editar mascota (M2). Obligatorios: nombre y especie; el resto en "Más detalles". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetFormScreen(
    petId: String?,
    onClose: () -> Unit,
    onSaved: () -> Unit = onClose,
    viewModel: PetFormViewModel = koinViewModel(key = "pet-form-$petId") { parametersOf(petId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val undo = LocalUndoController.current
    val restorePet = koinInject<RestorePet>()
    val deletedMessage = state.deletedPet?.let { stringResource(Res.string.pet_deleted, it.name) }
    val undoLabel = stringResource(Res.string.pet_undo)
    var confirmDelete by remember { mutableStateOf(false) }
    val birthPicker = rememberDatePickerLauncher(viewModel::onBirthDate)
    val photoPicker = rememberImagePickerLauncher(viewModel::onPhotoPicked)
    val openSettings = rememberOpenAppSettings()
    var choosePhoto by remember { mutableStateOf(false) }

    LaunchedEffect(state.done) {
        if (state.done) {
            val deleted = state.deletedPet
            if (deleted != null && deletedMessage != null) {
                undo.show(deletedMessage, undoLabel) { restorePet(deleted.id) }
                onClose()
            } else {
                onSaved()
            }
        }
    }

    FlowScaffold(
        title = stringResource(if (state.isEdit) Res.string.pet_form_edit_title else Res.string.pet_form_new_title),
        onClose = onClose,
        closeDescription = stringResource(Res.string.pet_close),
        confirmDiscard = state.dirty && !state.done,
        bottomAction = {
            if (state.saveFailed) SaveFailedCard()
            PrimaryButton(
                text = stringResource(Res.string.pet_form_save),
                onClick = viewModel::save,
                loading = state.saving || state.importingPhoto,
                enabled = state.loaded,
            )
        },
    ) {
        if (state.loadFailed) {
            LoadFailedState(onRetry = viewModel::retryLoad)
            return@FlowScaffold
        }
        if (!state.loaded) {
            LoadingState()
            return@FlowScaffold
        }
        PetPhotoPicker(
            photo = state.photoPath,
            species = (state.species ?: Species.OTHER).avatar(),
            name = state.name,
            actionLabel = stringResource(if (state.photoPath == null) Res.string.pet_photo_add else Res.string.pet_photo_change),
            onClick = { choosePhoto = true },
            loading = state.importingPhoto,
        )
        if (state.cameraDenied) CameraDeniedCard(onOpenSettings = openSettings)
        if (state.photoFailed > 0) PhotoFailedCard(state.photoFailed)
        FormField(
            label = stringResource(Res.string.pet_form_name),
            value = state.name,
            onValueChange = viewModel::onName,
            placeholder = stringResource(Res.string.pet_form_name_placeholder),
            error = when (state.nameError) {
                PetError.NAME_EMPTY -> stringResource(Res.string.pet_error_name_empty)
                PetError.NAME_TOO_LONG -> stringResource(Res.string.pet_error_name_long)
                null -> null
            },
        )
        Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            FieldLabelText(stringResource(Res.string.pet_form_species))
            FlowRow(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
            ) {
                speciesOptions.forEach { species ->
                    ChipChoice(
                        label = stringResource(species.label()),
                        selected = state.species == species,
                        onSelect = { viewModel.onSpecies(species) },
                    )
                }
            }
            if (state.speciesMissing) {
                FieldError(stringResource(Res.string.pet_error_species))
            }
        }
        ExpandableSection(title = stringResource(Res.string.pet_form_more), initiallyExpanded = state.isEdit) {
            FormField(
                label = stringResource(Res.string.pet_form_breed),
                value = state.breed,
                onValueChange = viewModel::onBreed,
                placeholder = stringResource(Res.string.pet_form_breed_placeholder),
                optional = true,
            )
            Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                FieldLabelText(stringResource(Res.string.pet_form_sex), optional = true)
                FlowRow(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                ) {
                    listOf(Sex.FEMALE, Sex.MALE).forEach { sex ->
                        ChipChoice(stringResource(sex.label()), state.sex == sex, onSelect = { viewModel.onSex(sex) })
                    }
                }
            }
            DateField(
                label = stringResource(Res.string.pet_form_birth),
                value = state.birthDate?.formatLong(),
                onClick = { birthPicker.launch(state.birthDate, max = null) },
                optional = true,
                allowApproximate = true,
                approximate = state.birthApproximate,
                onApproximateChange = viewModel::onBirthApproximate,
            )
            FormField(
                label = stringResource(Res.string.pet_form_marks),
                value = state.marks,
                onValueChange = viewModel::onMarks,
                placeholder = stringResource(Res.string.pet_form_marks_placeholder),
                optional = true,
                kind = FieldKind.MULTILINE,
            )
            FormField(stringResource(Res.string.pet_form_owner), state.owner, viewModel::onOwner, optional = true)
            FormField(
                label = stringResource(Res.string.pet_form_address),
                value = state.address,
                onValueChange = viewModel::onAddress,
                helper = stringResource(Res.string.pet_form_address_helper),
                optional = true,
            )
        }
        if (state.deleteFailed) DeleteFailedCard()
        if (state.isEdit) {
            TextAction(
                text = stringResource(Res.string.pet_form_delete),
                onClick = { confirmDelete = true },
                icon = BestIdIcons.Delete,
                destructive = true,
            )
        }
    }

    if (choosePhoto) {
        OptionsSheet(
            title = stringResource(Res.string.pet_photo_sheet),
            options = buildList {
                if (photoPicker.canTakePhoto) {
                    add(SheetOption(stringResource(UiRes.string.photo_take), BestIdIcons.PhotoCamera) { photoPicker.takePhoto() })
                }
                add(SheetOption(stringResource(UiRes.string.photo_gallery), BestIdIcons.Image) { photoPicker.pickFromGallery(1) })
                if (state.photoPath != null) {
                    add(SheetOption(stringResource(UiRes.string.photo_remove), BestIdIcons.Delete, destructive = true, onClick = viewModel::removePhoto))
                }
            },
            onDismiss = { choosePhoto = false },
        )
    }

    if (confirmDelete) {
        ConfirmSheet(
            title = stringResource(Res.string.pet_delete_title, state.name),
            body = stringResource(Res.string.pet_delete_body),
            confirmText = stringResource(Res.string.pet_delete_confirm),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
