package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.VaccineRecordCard
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.schedule.NextDue
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineType
import com.bestid.core.ui.daysText
import com.bestid.core.ui.formatDayMonth
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.vaccinesRegisteredText
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.cartilla_add_pet
import com.bestid.feature.cartilla.resources.cartilla_add_vaccine
import com.bestid.feature.cartilla.resources.cartilla_empty_body
import com.bestid.feature.cartilla.resources.cartilla_empty_title
import com.bestid.feature.cartilla.resources.cartilla_in_days
import com.bestid.feature.cartilla.resources.cartilla_next
import com.bestid.feature.cartilla.resources.cartilla_next_line
import com.bestid.feature.cartilla.resources.cartilla_no_pet_body
import com.bestid.feature.cartilla.resources.cartilla_no_pet_title
import com.bestid.feature.cartilla.resources.cartilla_overdue
import com.bestid.feature.cartilla.resources.cartilla_title
import com.bestid.feature.cartilla.resources.cartilla_to_confirm
import com.bestid.feature.cartilla.resources.cartilla_today
import com.bestid.feature.cartilla.resources.cartilla_vaccine_fallback
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Cartilla (C1): próxima dosis, registros por fecha y "Agregar vacuna". */
@Composable
fun CartillaScreen(
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onAddPet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartillaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pet = state.pet
    when {
        state.loading -> LoadingState(modifier = modifier.fillMaxSize())
        pet == null -> Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EmptyState(
                illustration = BestIdIcons.Vaccines,
                title = stringResource(Res.string.cartilla_no_pet_title),
                body = stringResource(Res.string.cartilla_no_pet_body),
                actionText = stringResource(Res.string.cartilla_add_pet),
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
            Column {
                Text(
                    stringResource(Res.string.cartilla_title),
                    style = BestIdTheme.typography.title,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    vaccinesRegisteredText(state.records.size),
                    style = BestIdTheme.typography.body,
                    color = BestIdTheme.colors.muted,
                )
            }
            if (state.records.isEmpty()) {
                EmptyState(
                    illustration = BestIdIcons.Vaccines,
                    title = stringResource(Res.string.cartilla_empty_title),
                    body = stringResource(Res.string.cartilla_empty_body, pet.name),
                )
            } else {
                state.nextDue?.let { NextDueCard(it, state.types, onClick = { onOpenRecord(it.record.id) }) }
                state.records.forEach { record ->
                    VaccineRecordCard(
                        date = record.applicationDate.formatLong(),
                        types = record.typeIds.labels(state.types) +
                            record.typeIdsToConfirm.labels(state.types).map { stringResource(Res.string.cartilla_to_confirm, it) },
                        onClick = { onOpenRecord(record.id) },
                        products = record.items.productsLine(),
                    )
                }
            }
            PrimaryButton(
                text = stringResource(Res.string.cartilla_add_vaccine),
                onClick = onAddRecord,
                icon = BestIdIcons.Add,
            )
        }
    }
}

/** Tarjeta "Próxima": durazno si faltan 7 días o menos (o está atrasada), verde si no. */
@Composable
internal fun NextDueCard(due: NextDue, types: Map<String, VaccineType>, onClick: (() -> Unit)? = null) {
    val name = due.record.nextDateLabel
        ?: due.record.typeIds.labels(types).firstOrNull()
        ?: stringResource(Res.string.cartilla_vaccine_fallback)
    val whenText = when {
        due.daysLeft == 0 -> stringResource(Res.string.cartilla_today)
        due.daysLeft > 0 -> stringResource(Res.string.cartilla_in_days, daysText(due.daysLeft))
        else -> stringResource(Res.string.cartilla_overdue, daysText(-due.daysLeft))
    }
    val content: @Composable () -> Unit = {
        Eyebrow(stringResource(Res.string.cartilla_next), color = if (due.daysLeft <= 7) BestIdTheme.colors.alertInk else BestIdTheme.colors.primaryInk)
        Text(stringResource(Res.string.cartilla_next_line, name, due.date.formatDayMonth()), style = BestIdTheme.typography.card)
        Text(whenText, style = BestIdTheme.typography.body)
    }
    if (due.daysLeft <= 7) {
        AlertCard(onClick = onClick) { content() }
    } else {
        InfoCard(icon = BestIdIcons.Calendar, onClick = onClick) { content() }
    }
}

internal fun List<VaccineItem>.productsLine(): String? =
    mapNotNull { it.productName?.takeIf(String::isNotBlank) }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
