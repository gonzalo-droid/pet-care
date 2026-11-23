package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.bestid.core.designsystem.component.Badge
import com.bestid.core.designsystem.component.BadgeTone
import com.bestid.core.designsystem.component.DateTile
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.SectionTitle
import com.bestid.core.designsystem.component.TimelineItem
import com.bestid.core.designsystem.theme.isLargeFont
import com.bestid.core.domain.usecase.groupedByYear
import com.bestid.core.ui.formatMonthShort
import com.bestid.feature.cartilla.resources.cartilla_summary
import com.bestid.feature.cartilla.resources.cartilla_open_record
import com.bestid.feature.cartilla.resources.cartilla_photos_count
import com.bestid.feature.cartilla.resources.cartilla_vaccines_count
import com.bestid.feature.cartilla.resources.cartilla_to_confirm_count
import com.bestid.feature.cartilla.resources.cartilla_year_to_confirm
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
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.VaccineRecordCard
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.domain.schedule.NextDue
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineType
import com.bestid.core.ui.daysText
import com.bestid.core.ui.formatLong
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.cartilla_add_pet
import com.bestid.feature.cartilla.resources.cartilla_add_vaccine
import com.bestid.feature.cartilla.resources.cartilla_share
import com.bestid.feature.cartilla.resources.cartilla_empty_body
import com.bestid.feature.cartilla.resources.cartilla_empty_title
import com.bestid.feature.cartilla.resources.cartilla_in_days
import com.bestid.feature.cartilla.resources.cartilla_next
import com.bestid.feature.cartilla.resources.cartilla_open_vaccine
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

/** Cartilla (C1): próxima dosis, historial por año en línea de tiempo y "Agregar vacuna". */
@Composable
fun CartillaScreen(
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onAddPet: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartillaViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pet = state.pet
    when {
        state.loading -> LoadingState(modifier = modifier.fillMaxSize())
        state.failed -> ErrorState(stringResource(UiRes.string.state_load_error), modifier = modifier.fillMaxSize(), onRetry = viewModel::retry)
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
                val vaccines = pluralStringResource(Res.plurals.cartilla_vaccines_count, state.records.size, state.records.size)
                Text(
                    if (state.toConfirm > 0) {
                        stringResource(
                            Res.string.cartilla_summary,
                            vaccines,
                            pluralStringResource(Res.plurals.cartilla_to_confirm_count, state.toConfirm, state.toConfirm),
                        )
                    } else {
                        vaccines
                    },
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
                state.records.groupedByYear().forEach { (year, records) ->
                    SectionTitle(year.toString())
                    // Sin espacio entre registros para que el riel se vea continuo: el aire va dentro de cada uno.
                    Column {
                        records.forEach { record ->
                            val date = record.applicationDate
                            TimelineItem(date = { DateTile(date.day.toString(), date.formatMonthShort(), date.formatLong()) }) {
                                VaccineRecordCard(
                                    date = null,
                                    types = record.typeIds.labels(state.types) +
                                        record.typeIdsToConfirm.labels(state.types).map { stringResource(Res.string.cartilla_to_confirm, it) },
                                    onClick = { onOpenRecord(record.id) },
                                    products = record.items.productsLine(),
                                    photos = record.sourcePhotoIds.size.takeIf { it > 0 }?.let {
                                        pluralStringResource(Res.plurals.cartilla_photos_count, it, it)
                                    },
                                    onClickLabel = stringResource(Res.string.cartilla_open_record, date.formatLong()),
                                    modifier = Modifier.padding(bottom = BestIdTheme.spacing.sm),
                                )
                            }
                        }
                    }
                }
            }
            PrimaryButton(
                text = stringResource(Res.string.cartilla_add_vaccine),
                onClick = onAddRecord,
                icon = BestIdIcons.Add,
            )
            if (state.records.isNotEmpty()) {
                SecondaryButton(text = stringResource(Res.string.cartilla_share), onClick = onShare, icon = BestIdIcons.Share)
            }
        }
    }
}

/** Tarjeta "Próxima": fecha en calendario (durazno si el año es dudoso), nombre y cuándo toca. */
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
    val tile: @Composable () -> Unit = {
        DateTile(due.date.day.toString(), due.date.formatMonthShort(), due.date.formatLong(), review = !due.confirmed)
    }
    val details: @Composable (Modifier) -> Unit = { m ->
        Column(m, verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs)) {
            Eyebrow(stringResource(Res.string.cartilla_next), color = if (due.daysLeft <= 7) BestIdTheme.colors.alertInk else BestIdTheme.colors.primaryInk)
            Text(name, style = BestIdTheme.typography.card, color = BestIdTheme.colors.ink)
            Text(whenText, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
            if (!due.confirmed) Badge(stringResource(Res.string.cartilla_year_to_confirm), BadgeTone.ALERT)
        }
    }
    PlainCard(onClick = onClick, onClickLabel = stringResource(Res.string.cartilla_open_vaccine), showChevron = true) {
        if (isLargeFont()) {
            tile()
            details(Modifier)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md)) {
                tile()
                details(Modifier.weight(1f))
            }
        }
    }
}

internal fun List<VaccineItem>.productsLine(): String? =
    mapNotNull { it.productName?.takeIf(String::isNotBlank) }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
