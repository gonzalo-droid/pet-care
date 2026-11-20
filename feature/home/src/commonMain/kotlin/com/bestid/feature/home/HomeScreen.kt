package com.bestid.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.platform.notification.rememberNotificationPermissionRequester
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.AvatarSize
import com.bestid.core.designsystem.component.Badge
import com.bestid.core.designsystem.component.BadgeTone
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PermissionCard
import com.bestid.core.designsystem.component.PetAvatar
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.AdaptivePair
import com.bestid.core.designsystem.component.StatCard
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.ui.avatar
import com.bestid.core.ui.displayName
import com.bestid.core.ui.formatWeight
import com.bestid.core.ui.daysText
import com.bestid.core.ui.formatDayMonth
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.vaccinesRegisteredText
import com.bestid.core.ui.speciesAndSex
import com.bestid.feature.home.resources.Res
import com.bestid.feature.home.resources.home_add_pet
import com.bestid.feature.home.resources.home_add_vaccine
import com.bestid.feature.home.resources.home_add_weight
import com.bestid.feature.home.resources.home_permission_body
import com.bestid.feature.home.resources.home_permission_title
import com.bestid.feature.home.resources.home_days_unit
import com.bestid.feature.home.resources.home_empty_body
import com.bestid.feature.home.resources.home_empty_title
import com.bestid.feature.home.resources.home_greeting_due
import com.bestid.feature.home.resources.home_greeting_ok
import com.bestid.feature.home.resources.home_greeting_overdue
import com.bestid.feature.home.resources.home_greeting_overdue_care
import com.bestid.feature.home.resources.home_greeting_start
import com.bestid.feature.home.resources.home_last_vaccine
import com.bestid.feature.home.resources.home_next
import com.bestid.feature.home.resources.home_next_line
import com.bestid.feature.home.resources.home_no_next
import com.bestid.feature.home.resources.home_no_next_body
import com.bestid.feature.home.resources.home_none
import com.bestid.feature.home.resources.home_open_detail
import com.bestid.feature.home.resources.home_open_vaccine
import com.bestid.feature.home.resources.home_overdue_days
import com.bestid.feature.home.resources.home_today
import com.bestid.feature.home.resources.home_vaccine_fallback
import com.bestid.feature.home.resources.home_weight
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Inicio (H1). El peso llega en M2; mientras tanto "Sin registro". */
@Composable
fun HomeScreen(
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onAddPet: () -> Unit,
    onOpenCare: (String) -> Unit,
    onAddWeight: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val requester = rememberNotificationPermissionRequester { viewModel.refreshPermission() }
    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermission()
        onPauseOrDispose { }
    }
    val pet = state.pet
    when {
        state.loading -> LoadingState(modifier = modifier.fillMaxSize())
        state.failed -> ErrorState(stringResource(UiRes.string.state_load_error), modifier = modifier.fillMaxSize(), onRetry = viewModel::retry)
        pet == null -> Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EmptyState(
                illustration = BestIdIcons.Pets,
                title = stringResource(Res.string.home_empty_title),
                body = stringResource(Res.string.home_empty_body),
                actionText = stringResource(Res.string.home_add_pet),
                onAction = onAddPet,
            )
        }
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
        ) {
            val due = state.upcoming
            val overdue = state.overdue
            val greeting = when {
                state.vaccineCount == 0 -> Res.string.home_greeting_start
                overdue is Upcoming.Vaccine -> Res.string.home_greeting_overdue
                overdue is Upcoming.Care -> Res.string.home_greeting_overdue_care
                due != null && due.daysLeft <= 7 -> Res.string.home_greeting_due
                else -> Res.string.home_greeting_ok
            }
            Text(
                stringResource(greeting, pet.name),
                style = BestIdTheme.typography.title,
                modifier = Modifier.semantics { heading() },
            )

            InfoCard(icon = null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
                ) {
                    PetAvatar(pet.photoPath, pet.species.avatar(), pet.name, size = AvatarSize.LARGE)
                    Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs)) {
                        Text(pet.name, style = BestIdTheme.typography.title, color = BestIdTheme.colors.ink)
                        Text(pet.speciesAndSex(), style = BestIdTheme.typography.body, color = BestIdTheme.colors.primaryInk)
                        Badge(
                            vaccinesRegisteredText(state.vaccineCount),
                            BadgeTone.NEUTRAL,
                        )
                    }
                }
            }

            if (state.remindersBlocked) {
                PermissionCard(
                    title = stringResource(Res.string.home_permission_title),
                    body = stringResource(Res.string.home_permission_body),
                    onOpenSettings = requester::openSettings,
                )
            }
            if (due != null) {
                NextDueHero(
                    due,
                    state.types,
                    onClick = {
                        when (due) {
                            is Upcoming.Vaccine -> onOpenRecord(due.record.id)
                            is Upcoming.Care -> onOpenCare(due.task.id)
                        }
                    },
                )
            } else if (state.vaccineCount > 0) {
                PlainCard {
                    Text(stringResource(Res.string.home_no_next), style = BestIdTheme.typography.card)
                    Text(stringResource(Res.string.home_no_next_body), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                }
            }

            val last = state.lastRecord
            val weight = state.lastWeight
            AdaptivePair(
                first = { cardModifier ->
                    StatCard(
                        label = stringResource(Res.string.home_last_vaccine),
                        value = last?.applicationDate?.formatLong() ?: stringResource(Res.string.home_none),
                        detail = last?.typeIds?.mapNotNull { state.types[it]?.label }?.joinToString(", "),
                        onClick = last?.let { { onOpenRecord(it.id) } },
                        onClickLabel = stringResource(Res.string.home_open_vaccine),
                        modifier = cardModifier,
                    )
                },
                second = { cardModifier ->
                    StatCard(
                        label = stringResource(Res.string.home_weight),
                        value = weight?.let { formatWeight(it.grams) } ?: stringResource(Res.string.home_none),
                        detail = weight?.date?.formatLong(),
                        actionText = if (weight == null) stringResource(Res.string.home_add_weight) else null,
                        onAction = if (weight == null) onAddWeight else null,
                        modifier = cardModifier,
                    )
                },
            )

            PrimaryButton(
                text = stringResource(Res.string.home_add_vaccine),
                onClick = onAddRecord,
                icon = BestIdIcons.Add,
            )
        }
    }
}

/** "PRÓXIMO · 49 días · Vacuna de leucemia · 31 dic", en durazno si faltan 7 días o menos. */
@Composable
private fun NextDueHero(due: Upcoming, types: Map<String, com.bestid.core.model.VaccineType>, onClick: () -> Unit) {
    val name = when (due) {
        is Upcoming.Vaccine -> due.record.nextDateLabel
            ?: due.record.typeIds.firstNotNullOfOrNull { types[it]?.label }
            ?: stringResource(Res.string.home_vaccine_fallback)
        is Upcoming.Care -> due.task.displayName()
    }
    val urgent = due.daysLeft <= 7
    val content: @Composable () -> Unit = {
        Eyebrow(stringResource(Res.string.home_next), color = if (urgent) BestIdTheme.colors.alertInk else BestIdTheme.colors.primaryInk)
        when {
            due.daysLeft == 0 -> Text(stringResource(Res.string.home_today), style = BestIdTheme.typography.hero, color = BestIdTheme.colors.ink)
            due.daysLeft < 0 -> Text(stringResource(Res.string.home_overdue_days, daysText(-due.daysLeft)), style = BestIdTheme.typography.subtitle, color = BestIdTheme.colors.ink)
            else -> Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                Text(due.daysLeft.toString(), style = BestIdTheme.typography.hero, color = BestIdTheme.colors.ink)
                Text(
                    pluralStringResource(Res.plurals.home_days_unit, due.daysLeft),
                    style = BestIdTheme.typography.subtitle,
                    color = BestIdTheme.colors.ink,
                    modifier = Modifier.padding(bottom = BestIdTheme.spacing.xxs),
                )
            }
        }
        Text(stringResource(Res.string.home_next_line, name, due.date.formatDayMonth()), style = BestIdTheme.typography.card, color = BestIdTheme.colors.ink)
    }
    val label = stringResource(Res.string.home_open_detail)
    if (urgent) {
        AlertCard(onClick = onClick, onClickLabel = label) { content() }
    } else {
        InfoCard(icon = null, onClick = onClick, onClickLabel = label) { content() }
    }
}
