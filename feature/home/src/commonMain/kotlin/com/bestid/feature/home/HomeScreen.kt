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
import com.bestid.core.designsystem.component.Badge
import com.bestid.core.designsystem.component.BadgeTone
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PermissionCard
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.AdaptivePair
import com.bestid.core.designsystem.component.StatCard
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.ui.displayName
import com.bestid.core.ui.formatWeight
import com.bestid.core.ui.daysText
import com.bestid.core.ui.formatLong
import androidx.compose.ui.semantics.contentDescription
import com.bestid.core.designsystem.component.CountdownRing
import com.bestid.core.designsystem.component.ReviewStrip
import com.bestid.core.designsystem.theme.isLargeFont
import com.bestid.core.domain.schedule.countdownProgress
import kotlinx.datetime.LocalDate
import com.bestid.feature.home.resources.Res
import com.bestid.feature.home.resources.home_summary
import com.bestid.feature.home.resources.home_next_a11y
import com.bestid.feature.home.resources.home_next_a11y_today
import com.bestid.feature.home.resources.home_next_a11y_unconfirmed
import com.bestid.feature.home.resources.home_next_a11y_overdue
import com.bestid.feature.home.resources.home_year_unreadable
import com.bestid.feature.home.resources.home_confirm
import com.bestid.feature.home.resources.home_year_to_confirm
import com.bestid.feature.home.resources.home_vaccines_count
import com.bestid.feature.home.resources.home_to_confirm_count
import com.bestid.feature.home.resources.home_add_pet
import com.bestid.feature.home.resources.home_add_vaccine
import com.bestid.feature.home.resources.home_add_weight
import com.bestid.feature.home.resources.home_permission_body
import com.bestid.feature.home.resources.home_permission_title
import com.bestid.feature.home.resources.home_days_unit
import com.bestid.feature.home.resources.home_days_overdue_unit
import com.bestid.feature.home.resources.home_empty_body
import com.bestid.feature.home.resources.home_empty_title
import com.bestid.feature.home.resources.home_greeting_due
import com.bestid.feature.home.resources.home_greeting_ok
import com.bestid.feature.home.resources.home_greeting_overdue
import com.bestid.feature.home.resources.home_greeting_overdue_care
import com.bestid.feature.home.resources.home_greeting_review
import com.bestid.feature.home.resources.home_greeting_start
import com.bestid.feature.home.resources.home_last_vaccine
import com.bestid.feature.home.resources.home_next
import com.bestid.feature.home.resources.home_no_next
import com.bestid.feature.home.resources.home_no_next_body
import com.bestid.feature.home.resources.home_none
import com.bestid.feature.home.resources.home_open_detail
import com.bestid.feature.home.resources.home_open_vaccine
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
    onEditRecord: (String) -> Unit,
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
                due is Upcoming.Vaccine && !due.record.nextDateConfirmed -> Res.string.home_greeting_review
                due != null && due.daysLeft <= 7 -> Res.string.home_greeting_due
                else -> Res.string.home_greeting_ok
            }
            Text(
                stringResource(greeting, pet.name),
                style = BestIdTheme.typography.title,
                modifier = Modifier.semantics { heading() },
            )

            // Con 0 vacunas el saludo ya pide registrarlas; el contador "0 vacunas" sobraba.
            if (state.vaccineCount > 0) {
                val vaccines = pluralStringResource(Res.plurals.home_vaccines_count, state.vaccineCount, state.vaccineCount)
                Text(
                    if (state.toConfirm > 0) {
                        stringResource(
                            Res.string.home_summary,
                            vaccines,
                            pluralStringResource(Res.plurals.home_to_confirm_count, state.toConfirm, state.toConfirm),
                        )
                    } else {
                        vaccines
                    },
                    style = BestIdTheme.typography.body,
                    color = BestIdTheme.colors.muted,
                )
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
                    state.today,
                    onClick = {
                        when (due) {
                            is Upcoming.Vaccine -> onOpenRecord(due.record.id)
                            is Upcoming.Care -> onOpenCare(due.task.id)
                        }
                    },
                )
                if (due is Upcoming.Vaccine && !due.record.nextDateConfirmed) {
                    ReviewStrip(
                        text = stringResource(Res.string.home_year_unreadable),
                        actionText = stringResource(Res.string.home_confirm),
                        onAction = { onEditRecord(due.record.id) },
                    )
                }
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
                        icon = BestIdIcons.Vaccines,
                        label = stringResource(Res.string.home_last_vaccine),
                        value = last?.applicationDate?.formatLong() ?: stringResource(Res.string.home_none),
                        detail = last?.typeIds?.mapNotNull { state.types[it]?.label }?.joinToString(", "),
                        onClick = last?.let { { onOpenRecord(it.id) } },
                        onClickLabel = stringResource(Res.string.home_open_vaccine),
                        actionText = if (last == null) stringResource(Res.string.home_add_vaccine) else null,
                        onAction = if (last == null) onAddRecord else null,
                        modifier = cardModifier,
                    )
                },
                second = { cardModifier ->
                    StatCard(
                        icon = BestIdIcons.Weight,
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

/** "PRÓXIMO", anillo con los días que faltan, nombre y fecha. Se lee como un solo bloque. */
@Composable
private fun NextDueHero(due: Upcoming, types: Map<String, com.bestid.core.model.VaccineType>, today: LocalDate?, onClick: () -> Unit) {
    val name = when (due) {
        is Upcoming.Vaccine -> due.record.nextDateLabel
            ?: due.record.typeIds.firstNotNullOfOrNull { types[it]?.label }
            ?: stringResource(Res.string.home_vaccine_fallback)
        is Upcoming.Care -> due.task.displayName()
    }
    val urgent = due.daysLeft <= 7
    val longDate = due.date.formatLong()
    val overdue = due.daysLeft < 0
    val ringValue = when {
        due.daysLeft == 0 -> stringResource(Res.string.home_today)
        overdue -> (-due.daysLeft).toString()
        else -> due.daysLeft.toString()
    }
    val ringUnit = when {
        due.daysLeft == 0 -> ""
        overdue -> pluralStringResource(Res.plurals.home_days_overdue_unit, -due.daysLeft)
        else -> pluralStringResource(Res.plurals.home_days_unit, due.daysLeft)
    }
    val progress = if (overdue) null else today?.let { countdownProgress(due.from, due.date, it) }
    val unconfirmed = due is Upcoming.Vaccine && !due.record.nextDateConfirmed
    val spoken = when {
        due.daysLeft == 0 -> stringResource(Res.string.home_next_a11y_today, name, longDate)
        overdue -> stringResource(Res.string.home_next_a11y_overdue, name, longDate, daysText(-due.daysLeft))
        else -> stringResource(Res.string.home_next_a11y, name, longDate, daysText(due.daysLeft))
    }
    val a11y = if (unconfirmed) stringResource(Res.string.home_next_a11y_unconfirmed, spoken) else spoken
    val ring: @Composable () -> Unit = { CountdownRing(ringValue, ringUnit, progress) }
    val details: @Composable (Modifier) -> Unit = { m ->
        Column(m, verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            Text(name, style = BestIdTheme.typography.card, color = BestIdTheme.colors.ink)
            Text(longDate, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
            if (unconfirmed) {
                Badge(stringResource(Res.string.home_year_to_confirm), BadgeTone.ALERT)
            }
        }
    }
    PlainCard(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = a11y },
        onClick = onClick,
        onClickLabel = stringResource(Res.string.home_open_detail),
        showChevron = true,
    ) {
        Eyebrow(stringResource(Res.string.home_next), color = if (urgent) BestIdTheme.colors.alertInk else BestIdTheme.colors.primaryInk)
        if (isLargeFont()) {
            ring()
            details(Modifier)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md)) {
                ring()
                details(Modifier.weight(1f))
            }
        }
    }
}
