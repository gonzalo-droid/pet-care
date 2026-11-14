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
import com.bestid.core.designsystem.component.DashedButton
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PetIdCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.model.DatePrecision
import com.bestid.core.ui.avatar
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.label
import com.bestid.feature.pet.resources.Res
import com.bestid.feature.pet.resources.pet_add
import com.bestid.feature.pet.resources.pet_add_another
import com.bestid.feature.pet.resources.pet_complete_data
import com.bestid.feature.pet.resources.pet_share
import com.bestid.feature.pet.resources.pet_empty_body
import com.bestid.feature.pet.resources.pet_empty_title
import com.bestid.feature.pet.resources.pet_next_none
import com.bestid.feature.pet.resources.pet_row_birth
import com.bestid.feature.pet.resources.pet_row_birth_approx
import com.bestid.feature.pet.resources.pet_row_breed
import com.bestid.feature.pet.resources.pet_row_marks
import com.bestid.feature.pet.resources.pet_row_owner
import com.bestid.feature.pet.resources.pet_row_sex
import com.bestid.feature.pet.resources.pet_row_species
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
                stringResource(Res.string.pet_row_species) to stringResource(pet.species.label(pet.sex)),
                pet.sex?.let { stringResource(Res.string.pet_row_sex) to stringResource(it.label()) },
                pet.breed?.let { stringResource(Res.string.pet_row_breed) to it },
                birth?.let { stringResource(Res.string.pet_row_birth) to it },
                pet.distinguishingMarks?.let { stringResource(Res.string.pet_row_marks) to it },
                pet.ownerName?.let { stringResource(Res.string.pet_row_owner) to it },
            )
            PetIdCard(
                name = pet.name,
                photo = pet.photoPath,
                species = pet.species.avatar(),
                details = details,
                vaccineCount = state.vaccineCount.toString(),
                nextDate = state.nextDue?.date?.formatLong() ?: stringResource(Res.string.pet_next_none),
            )
            PrimaryButton(text = stringResource(Res.string.pet_share), onClick = onShare, icon = BestIdIcons.Share)
            SecondaryButton(
                text = stringResource(Res.string.pet_complete_data),
                onClick = { onEditPet(pet.id) },
                icon = BestIdIcons.Edit,
            )
            DashedButton(text = stringResource(Res.string.pet_add_another), onClick = onAddPet)
        }
    }
}
