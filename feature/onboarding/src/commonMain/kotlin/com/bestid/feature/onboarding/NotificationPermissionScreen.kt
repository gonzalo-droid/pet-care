package com.bestid.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bestid.core.designsystem.component.CelebrationBadge
import com.bestid.core.designsystem.component.NotificationPreview
import com.bestid.core.designsystem.component.FillScrollColumn
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.reminder.MarkNotificationPromptShown
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.model.Pet
import com.bestid.core.platform.notification.rememberNotificationPermissionRequester
import com.bestid.feature.onboarding.resources.Res
import com.bestid.feature.onboarding.resources.permission_accept
import com.bestid.feature.onboarding.resources.permission_body
import com.bestid.feature.onboarding.resources.permission_celebration_care
import com.bestid.feature.onboarding.resources.permission_celebration_vaccines
import com.bestid.feature.onboarding.resources.permission_example_body
import com.bestid.feature.onboarding.resources.permission_example_title
import com.bestid.feature.onboarding.resources.permission_footnote
import com.bestid.feature.onboarding.resources.permission_later
import com.bestid.feature.onboarding.resources.permission_pet_fallback
import com.bestid.feature.onboarding.resources.permission_title
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class PermissionState(val pet: Pet? = null, val vaccineCount: Int = 0)

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationPermissionViewModel(
    observeSelectedPet: ObserveSelectedPet,
    observeRecords: ObserveVaccineRecords,
    markShown: MarkNotificationPromptShown,
) : ViewModel() {
    val state: StateFlow<PermissionState> = observeSelectedPet()
        .flatMapLatest { pet ->
            if (pet == null) flowOf(PermissionState()) else observeRecords(pet.id).map { PermissionState(pet, it.size) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PermissionState())

    init {
        // Se muestra una sola vez, conteste lo que conteste.
        viewModelScope.launch { markShown() }
    }
}

/**
 * Permiso de avisos (O5), después del primer logro: celebra lo guardado, muestra un ejemplo del
 * aviso y recién entonces pide el permiso del sistema. "Ahora no" está permitido.
 */
@Composable
fun NotificationPermissionScreen(onDone: () -> Unit, viewModel: NotificationPermissionViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val requester = rememberNotificationPermissionRequester { onDone() }
    val name = state.pet?.name ?: stringResource(Res.string.permission_pet_fallback)

    FillScrollColumn(
        bottom = {
            PrimaryButton(stringResource(Res.string.permission_accept), onClick = requester::request)
            TextAction(stringResource(Res.string.permission_later), onClick = onDone)
            Text(
                stringResource(Res.string.permission_footnote),
                style = BestIdTheme.typography.support,
                color = BestIdTheme.colors.muted,
                textAlign = TextAlign.Center,
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(BestIdTheme.colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CelebrationBadge(
            if (state.vaccineCount > 0) {
                pluralStringResource(Res.plurals.permission_celebration_vaccines, state.vaccineCount, name, state.vaccineCount)
            } else {
                stringResource(Res.string.permission_celebration_care, name)
            },
        )
        Text(
            stringResource(Res.string.permission_title),
            style = BestIdTheme.typography.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(Res.string.permission_body),
            style = BestIdTheme.typography.body,
            color = BestIdTheme.colors.muted,
            textAlign = TextAlign.Center,
        )
        NotificationPreview(
            title = stringResource(Res.string.permission_example_title, name),
            body = stringResource(Res.string.permission_example_body, name),
        )
    }
}
