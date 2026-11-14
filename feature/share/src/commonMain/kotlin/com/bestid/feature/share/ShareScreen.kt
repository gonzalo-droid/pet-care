package com.bestid.feature.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.AvatarSize
import com.bestid.core.designsystem.component.CheckRow
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PetAvatar
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SectionTitle
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.share.BuildPetSheet
import com.bestid.core.domain.share.PetSheet
import com.bestid.core.domain.share.SheetOptions
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ShareFilePaths
import com.bestid.core.navigation.ShareRoute
import com.bestid.core.platform.pdf.PdfColors
import com.bestid.core.platform.pdf.PdfRenderer
import com.bestid.core.platform.share.MimeTypes
import com.bestid.core.platform.share.rememberShareSheet
import com.bestid.core.ui.avatar
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.label
import com.bestid.feature.share.resources.Res
import com.bestid.feature.share.resources.pdf_file_name
import com.bestid.feature.share.resources.share_button
import com.bestid.feature.share.resources.share_card_photos
import com.bestid.feature.share.resources.share_close
import com.bestid.feature.share.resources.share_error
import com.bestid.feature.share.resources.share_hint
import com.bestid.feature.share.resources.share_include
import com.bestid.feature.share.resources.share_no_address
import com.bestid.feature.share.resources.share_no_pet
import com.bestid.feature.share.resources.share_no_vaccines_preview
import com.bestid.feature.share.resources.share_owner
import com.bestid.feature.share.resources.share_preview
import com.bestid.feature.share.resources.share_title
import com.bestid.feature.share.resources.share_vaccine_count
import com.bestid.feature.share.resources.share_vaccines
import com.bestid.feature.share.resources.share_weight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

data class ShareState(
    val loaded: Boolean = false,
    val options: SheetOptions = SheetOptions(),
    /** Vista previa con las opciones actuales; `null` si no hay mascota. */
    val sheet: PetSheet? = null,
    val generating: Boolean = false,
    val failed: Boolean = false,
    /** PDF listo para la hoja de compartir; la pantalla lo consume una vez. */
    val readyPath: String? = null,
)

/** Compartir ficha (SH1) de la mascota seleccionada. */
class ShareViewModel(
    private val observeSelectedPet: ObserveSelectedPet,
    private val buildSheet: BuildPetSheet,
    private val renderer: PdfRenderer,
    private val paths: ShareFilePaths,
) : ViewModel() {
    private val _state = MutableStateFlow(ShareState())
    val state: StateFlow<ShareState> = _state.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            val pet = observeSelectedPet().first()
            val sheet = pet?.let { buildSheet(it.id, _state.value.options) }
            _state.update { it.copy(loaded = true, sheet = sheet) }
        }
    }

    fun setOptions(options: SheetOptions) {
        _state.update { it.copy(options = options) }
        refresh()
    }

    fun generate(colors: PdfColors) {
        val sheet = _state.value.sheet ?: return
        if (_state.value.generating) return
        _state.update { it.copy(generating = true, failed = false) }
        viewModelScope.launch {
            val result = runCatching {
                val target = paths.target(getString(Res.string.pdf_file_name, safeFileName(sheet.pet.name)))
                renderer.render(sheet.toPdf(colors, paths::photo), target)
                target
            }
            _state.update { it.copy(generating = false, failed = result.isFailure, readyPath = result.getOrNull()) }
        }
    }

    fun consumeReady() = _state.update { it.copy(readyPath = null) }
}

@Composable
fun ShareScreen(onClose: () -> Unit, viewModel: ShareViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shareSheet = rememberShareSheet()
    val title = stringResource(Res.string.share_title)
    val c = BestIdTheme.colors
    val colors = PdfColors(c.primary.toArgb(), c.onPrimary.toArgb(), c.ink.toArgb(), c.muted.toArgb(), c.line.toArgb(), c.primarySoft.toArgb())

    LaunchedEffect(state.readyPath) {
        state.readyPath?.let {
            shareSheet.share(it, MimeTypes.PDF, title)
            viewModel.consumeReady()
        }
    }

    FlowScaffold(
        title = title,
        onClose = onClose,
        closeDescription = stringResource(Res.string.share_close),
        bottomAction = {
            Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs), horizontalAlignment = Alignment.CenterHorizontally) {
                PrimaryButton(
                    text = stringResource(Res.string.share_button),
                    onClick = { viewModel.generate(colors) },
                    loading = state.generating,
                    enabled = state.sheet != null,
                    icon = BestIdIcons.Share,
                )
                Text(stringResource(Res.string.share_hint), style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted, textAlign = TextAlign.Center)
            }
        },
    ) {
        val sheet = state.sheet
        when {
            !state.loaded -> LoadingState()
            sheet == null -> AlertCard { Text(stringResource(Res.string.share_no_pet), style = BestIdTheme.typography.body) }
            else -> {
                Preview(sheet)
                SectionTitle(stringResource(Res.string.share_include))
                PlainCard {
                    val o = state.options
                    CheckRow(stringResource(Res.string.share_vaccines), o.includeVaccines, { viewModel.setOptions(o.copy(includeVaccines = it)) })
                    HorizontalDivider(color = BestIdTheme.colors.line)
                    CheckRow(stringResource(Res.string.share_card_photos), o.includeCardPhotos, { viewModel.setOptions(o.copy(includeCardPhotos = it)) })
                    HorizontalDivider(color = BestIdTheme.colors.line)
                    CheckRow(stringResource(Res.string.share_weight), o.includeWeight, { viewModel.setOptions(o.copy(includeWeight = it)) })
                    HorizontalDivider(color = BestIdTheme.colors.line)
                    CheckRow(stringResource(Res.string.share_owner), o.includeOwnerName, { viewModel.setOptions(o.copy(includeOwnerName = it)) })
                }
                Text(stringResource(Res.string.share_no_address), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                if (state.failed) AlertCard { Text(stringResource(Res.string.share_error), style = BestIdTheme.typography.body) }
            }
        }
    }
}

/** Vista previa del prototipo: mascota y las vacunas que irán en la tabla. */
@Composable
private fun Preview(sheet: PetSheet) {
    val pet = sheet.pet
    PlainCard {
        Eyebrow(stringResource(Res.string.share_preview))
        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            PetAvatar(pet.photoPath, pet.species.avatar(), pet.name, size = AvatarSize.MEDIUM)
            Column {
                Text(pet.name, style = BestIdTheme.typography.title)
                val parts = listOfNotNull(
                    stringResource(pet.species.label(pet.sex)),
                    pet.sex?.let { stringResource(it.label()) },
                    pluralStringResource(Res.plurals.share_vaccine_count, sheet.vaccines.size, sheet.vaccines.size),
                )
                Text(parts.joinToString(" · "), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
            }
        }
        if (sheet.vaccines.isEmpty()) {
            Text(stringResource(Res.string.share_no_vaccines_preview), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
        }
        sheet.vaccines.take(5).forEach { v ->
            Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
                Text(v.record.applicationDate.formatLong(), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                Text(
                    v.typeLabels.joinToString(", "),
                    style = BestIdTheme.typography.bodyStrong,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

fun NavGraphBuilder.shareFlows(onClose: () -> Unit) {
    composable<ShareRoute> { ShareScreen(onClose = onClose) }
}

val shareModule = module {
    viewModelOf(::ShareViewModel)
}
