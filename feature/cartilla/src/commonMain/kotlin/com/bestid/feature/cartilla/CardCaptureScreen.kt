package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bestid.core.designsystem.component.AddPageTile
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.OptionsSheet
import com.bestid.core.designsystem.component.PageTile
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.SheetOption
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.model.Photo
import com.bestid.core.platform.image.ImagePickerLauncher
import com.bestid.core.platform.image.ImagePickerResult
import com.bestid.core.platform.image.MAX_CARD_PAGES
import com.bestid.core.platform.image.rememberImagePickerLauncher
import com.bestid.core.platform.image.rememberOpenAppSettings
import com.bestid.core.ui.CameraDeniedCard
import com.bestid.core.ui.PhotoFailedCard
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.photo_gallery
import com.bestid.core.ui.resources.photo_remove
import com.bestid.core.ui.resources.photo_take
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.capture_by_hand
import com.bestid.feature.cartilla.resources.capture_continue
import com.bestid.feature.cartilla.resources.capture_first_page
import com.bestid.feature.cartilla.resources.capture_gallery
import com.bestid.feature.cartilla.resources.capture_headline
import com.bestid.feature.cartilla.resources.capture_importing
import com.bestid.feature.cartilla.resources.capture_limit
import com.bestid.feature.cartilla.resources.capture_other_page
import com.bestid.feature.cartilla.resources.capture_take
import com.bestid.feature.cartilla.resources.capture_tip_dates
import com.bestid.feature.cartilla.resources.capture_tip_light
import com.bestid.feature.cartilla.resources.capture_tip_page
import com.bestid.feature.cartilla.resources.capture_tips_title
import com.bestid.feature.cartilla.resources.capture_title
import com.bestid.feature.cartilla.resources.form_close
import com.bestid.feature.cartilla.resources.photo_add_sheet
import com.bestid.feature.cartilla.resources.photo_page
import com.bestid.feature.cartilla.resources.photo_page_description
import com.bestid.feature.cartilla.resources.photo_page_sheet
import com.bestid.feature.cartilla.resources.photo_remove_page
import com.bestid.feature.cartilla.resources.photo_sticker_description
import com.bestid.feature.cartilla.resources.photo_view
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class CaptureState(
    val pages: List<Photo> = emptyList(),
    val importing: Boolean = false,
    val failed: Int = 0,
    val cameraDenied: Boolean = false,
) {
    val remaining: Int get() = MAX_CARD_PAGES - pages.size
}

/** Fotos de la cartilla (O2). Las páginas se procesan y guardan al elegirlas. */
class CardCaptureViewModel(private val importPhotos: ImportPhotos) : ViewModel() {
    private val _state = MutableStateFlow(CaptureState())
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    fun onPicked(result: ImagePickerResult) {
        when (result) {
            ImagePickerResult.CameraDenied -> _state.update { it.copy(cameraDenied = true) }
            is ImagePickerResult.Picked -> {
                val images = result.images.take(_state.value.remaining)
                if (images.isEmpty()) return
                _state.update { it.copy(importing = true, failed = 0, cameraDenied = false) }
                viewModelScope.launch {
                    val imported = importPhotos(images)
                    _state.update { it.copy(importing = false, pages = it.pages + imported.photos, failed = imported.failed) }
                }
            }
        }
    }

    fun remove(id: String) = _state.update { s -> s.copy(pages = s.pages.filterNot { it.id == id }) }
}

/**
 * Fotos de la cartilla (O2): hasta 4 páginas con cámara o galería. Sin lectura automática por
 * ahora (D29): "Continuar" abre el formulario de vacuna con las fotos para llenarlo viéndolas.
 */
@Composable
fun CardCaptureScreen(
    onClose: () -> Unit,
    onContinue: (photoIds: List<String>) -> Unit,
    onByHand: () -> Unit,
    onOpenPhoto: (path: String) -> Unit,
    viewModel: CardCaptureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberImagePickerLauncher(viewModel::onPicked)
    val openSettings = rememberOpenAppSettings()
    var sheet by remember { mutableStateOf<PhotoSheet?>(null) }

    FlowScaffold(
        title = stringResource(Res.string.capture_title),
        onClose = onClose,
        closeDescription = stringResource(Res.string.form_close),
        bottomAction = {
            Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                if (state.pages.isNotEmpty()) {
                    PrimaryButton(
                        text = pluralStringResource(Res.plurals.capture_continue, state.pages.size, state.pages.size),
                        onClick = { onContinue(state.pages.map { it.id }) },
                        enabled = !state.importing,
                    )
                }
                if (state.remaining > 0) {
                    SourceButtons(picker, state.remaining, enabled = !state.importing)
                }
                TextAction(stringResource(Res.string.capture_by_hand), onClick = onByHand, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        },
    ) {
        Text(stringResource(Res.string.capture_headline), style = BestIdTheme.typography.title, modifier = Modifier.semantics { heading() })
        Text(stringResource(Res.string.capture_limit), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)

        val tiles: List<Photo?> = state.pages + if (state.remaining > 0) listOf(null) else emptyList()
        tiles.chunked(3).forEachIndexed { row, chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
                chunk.forEachIndexed { column, page ->
                    val number = row * 3 + column + 1
                    val m = Modifier.weight(1f)
                    if (page != null) {
                        PageTile(
                            image = page.path,
                            label = stringResource(Res.string.photo_page, number),
                            contentDescription = stringResource(Res.string.photo_page_description, number),
                            onClick = { sheet = PhotoSheet.Page(page, number) },
                            modifier = m,
                        )
                    } else {
                        AddPageTile(
                            text = stringResource(if (state.pages.isEmpty()) Res.string.capture_first_page else Res.string.capture_other_page),
                            onClick = { sheet = PhotoSheet.AddPage },
                            enabled = !state.importing,
                            modifier = m,
                        )
                    }
                }
                repeat(3 - chunk.size) { Box(Modifier.weight(1f)) }
            }
        }
        if (state.importing) LoadingState(text = stringResource(Res.string.capture_importing))
        if (state.cameraDenied) CameraDeniedCard(onOpenSettings = openSettings)
        if (state.failed > 0) PhotoFailedCard(state.failed)

        PlainCard {
            Text(stringResource(Res.string.capture_tips_title), style = BestIdTheme.typography.bodyStrong)
            listOf(Res.string.capture_tip_light, Res.string.capture_tip_dates, Res.string.capture_tip_page).forEach { tip ->
                Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm), verticalAlignment = Alignment.Top) {
                    Icon(painterResource(BestIdIcons.Check), contentDescription = null, tint = BestIdTheme.colors.primary)
                    Text(stringResource(tip), style = BestIdTheme.typography.body)
                }
            }
        }
    }

    PhotoSheets(
        sheet = sheet,
        picker = picker,
        maxPages = state.remaining,
        onDismiss = { sheet = null },
        onOpenPhoto = onOpenPhoto,
        onRemovePage = { viewModel.remove(it.id) },
    )
}

/** "Tomar foto" y "Galería" lado a lado, como en el prototipo. */
@Composable
internal fun SourceButtons(picker: ImagePickerLauncher, max: Int, enabled: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
        if (picker.canTakePhoto) {
            SecondaryButton(stringResource(Res.string.capture_take), onClick = picker::takePhoto, enabled = enabled, modifier = Modifier.weight(1f))
        }
        SecondaryButton(stringResource(Res.string.capture_gallery), onClick = { picker.pickFromGallery(max) }, enabled = enabled, modifier = Modifier.weight(1f))
    }
}

/** Hojas de fotos que comparten la captura, el formulario y el detalle. */
internal sealed interface PhotoSheet {
    data object AddPage : PhotoSheet
    data object AddSticker : PhotoSheet
    data class Page(val photo: Photo, val number: Int) : PhotoSheet
    data class Sticker(val photo: Photo) : PhotoSheet
}

@Composable
internal fun PhotoSheets(
    sheet: PhotoSheet?,
    picker: ImagePickerLauncher,
    maxPages: Int,
    onDismiss: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onRemovePage: (Photo) -> Unit = {},
    onRemoveSticker: () -> Unit = {},
) {
    when (sheet) {
        null -> Unit
        PhotoSheet.AddPage, PhotoSheet.AddSticker -> OptionsSheet(
            title = stringResource(if (sheet == PhotoSheet.AddPage) Res.string.photo_add_sheet else Res.string.photo_sticker_description),
            options = sourceOptions(picker, if (sheet == PhotoSheet.AddPage) maxPages else 1),
            onDismiss = onDismiss,
        )
        is PhotoSheet.Page -> OptionsSheet(
            title = stringResource(Res.string.photo_page_sheet, sheet.number),
            options = listOf(
                SheetOption(stringResource(Res.string.photo_view), BestIdIcons.Image) { onOpenPhoto(sheet.photo.path) },
                SheetOption(stringResource(Res.string.photo_remove_page), BestIdIcons.Delete, destructive = true) { onRemovePage(sheet.photo) },
            ),
            onDismiss = onDismiss,
        )
        is PhotoSheet.Sticker -> OptionsSheet(
            title = stringResource(Res.string.photo_sticker_description),
            options = listOf(
                SheetOption(stringResource(Res.string.photo_view), BestIdIcons.Image) { onOpenPhoto(sheet.photo.path) },
                SheetOption(stringResource(UiRes.string.photo_remove), BestIdIcons.Delete, destructive = true, onClick = onRemoveSticker),
            ),
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun sourceOptions(picker: ImagePickerLauncher, max: Int): List<SheetOption> = buildList {
    if (picker.canTakePhoto) add(SheetOption(stringResource(UiRes.string.photo_take), BestIdIcons.PhotoCamera) { picker.takePhoto() })
    add(SheetOption(stringResource(UiRes.string.photo_gallery), BestIdIcons.Image) { picker.pickFromGallery(max) })
}
