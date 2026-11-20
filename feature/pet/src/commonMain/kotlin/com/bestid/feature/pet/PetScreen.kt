package com.bestid.feature.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.ProgressRow
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PetIdCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.model.DatePrecision
import com.bestid.core.ui.avatar
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.joinWithAnd
import com.bestid.core.ui.speciesAndSex
import com.bestid.core.domain.usecase.PetField
import com.bestid.core.domain.usecase.missingFields
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import com.bestid.feature.pet.resources.Res
import com.bestid.feature.pet.resources.pet_add
import com.bestid.feature.pet.resources.pet_add_another
import com.bestid.feature.pet.resources.pet_complete
import com.bestid.feature.pet.resources.pet_missing
import com.bestid.feature.pet.resources.pet_missing_birth
import com.bestid.feature.pet.resources.pet_missing_breed
import com.bestid.feature.pet.resources.pet_missing_marks
import com.bestid.feature.pet.resources.pet_missing_owner
import com.bestid.feature.pet.resources.pet_vaccines_count
import com.bestid.feature.pet.resources.pet_share
import com.bestid.feature.pet.resources.pet_empty_body
import com.bestid.feature.pet.resources.pet_empty_title
import com.bestid.feature.pet.resources.pet_next_none
import com.bestid.feature.pet.resources.pet_row_birth
import com.bestid.feature.pet.resources.pet_row_birth_approx
import com.bestid.feature.pet.resources.pet_row_breed
import com.bestid.feature.pet.resources.pet_row_marks
import com.bestid.feature.pet.resources.pet_row_owner
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Pestaña Mascota (M1): carnet BestID de la mascota seleccionada. */
@Composable
fun PetScreen(
    onEditPet: (String) -> Unit,
    onAddPet: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PetTabViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pet = state.pet
    when {
        state.loading -> LoadingState(modifier = modifier.fillMaxSize())
        state.failed -> ErrorState(stringResource(UiRes.string.state_load_error), modifier = modifier.fillMaxSize(), onRetry = viewModel::retry)
        pet == null -> Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EmptyState(
                illustration = BestIdIcons.PetsFilled,
                title = stringResource(Res.string.pet_empty_title),
                body = stringResource(Res.string.pet_empty_body),
                actionText = stringResource(Res.string.pet_add),
                onAction = onAddPet,
            )
        }
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
        ) {
            val birth = pet.birthDate?.formatLong()?.let { date ->
                if (pet.birthDatePrecision != null && pet.birthDatePrecision != DatePrecision.EXACT) {
                    stringResource(Res.string.pet_row_birth_approx, date)
                } else date
            }
            val details = listOfNotNull(
                pet.breed?.takeIf(String::isNotBlank)?.let { stringResource(Res.string.pet_row_breed) to it },
                birth?.let { stringResource(Res.string.pet_row_birth) to it },
                pet.distinguishingMarks?.takeIf(String::isNotBlank)?.let { stringResource(Res.string.pet_row_marks) to it },
                pet.ownerName?.takeIf(String::isNotBlank)?.let { stringResource(Res.string.pet_row_owner) to it },
            )
            val missing = pet.missingFields()
            val missingText = missing.map { stringResource(it.missingLabel()) }.joinWithAnd()
            PetIdCard(
                name = pet.name,
                photo = pet.photoPath,
                species = pet.species.avatar(),
                subtitle = pet.speciesAndSex(),
                details = details,
                vaccineCount = pluralStringResource(Res.plurals.pet_vaccines_count, state.vaccineCount, state.vaccineCount),
                nextDate = state.nextDue?.date?.formatLong() ?: stringResource(Res.string.pet_next_none),
                completion = if (missing.isEmpty()) null else {
                    {
                        ProgressRow(
                            text = pluralStringResource(Res.plurals.pet_missing, missing.size, missingText),
                            progress = (PetField.entries.size - missing.size) / PetField.entries.size.toFloat(),
                            actionText = stringResource(Res.string.pet_complete),
                            onClick = { onEditPet(pet.id) },
                        )
                    }
                },
            )
            PrimaryButton(text = stringResource(Res.string.pet_share), onClick = onShare, icon = BestIdIcons.Share)
            TextAction(
                text = stringResource(Res.string.pet_add_another),
                onClick = onAddPet,
                icon = BestIdIcons.Add,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

private fun PetField.missingLabel(): StringResource = when (this) {
    PetField.BREED -> Res.string.pet_missing_breed
    PetField.BIRTH_DATE -> Res.string.pet_missing_birth
    PetField.MARKS -> Res.string.pet_missing_marks
    PetField.OWNER -> Res.string.pet_missing_owner
}
