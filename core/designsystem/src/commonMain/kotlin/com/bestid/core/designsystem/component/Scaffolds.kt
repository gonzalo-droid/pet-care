package com.bestid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_close
import com.bestid.core.designsystem.theme.AppTheme
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Pestaña de la [BottomBar]. */
@Immutable
data class BottomBarItem(
    val label: String,
    val icon: DrawableResource,
    val selectedIcon: DrawableResource,
)

/**
 * Barra inferior de 4 pestañas (Inicio, Cartilla, Salud, Mascota). La pestaña activa cambia
 * de ícono (relleno), de peso y de fondo: nunca solo de color. Con texto grande la barra crece.
 */
@Composable
fun BottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BestIdTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface)) {
        HorizontalDivider(color = colors.line)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = BestIdTheme.spacing.xs, vertical = BestIdTheme.spacing.xxs)
                .selectableGroup(),
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = BestIdTheme.sizes.minTouch)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(vertical = BestIdTheme.spacing.xxs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (selected) colors.primarySoft else colors.surface, BestIdTheme.shapes.pill)
                            .padding(horizontal = BestIdTheme.spacing.md, vertical = BestIdTheme.spacing.xxs),
                    ) {
                        Icon(
                            painterResource(if (selected) item.selectedIcon else item.icon),
                            contentDescription = null,
                            tint = if (selected) colors.primaryInk else colors.muted,
                        )
                    }
                    Text(
                        item.label,
                        style = if (selected) BestIdTheme.typography.label.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        ) else BestIdTheme.typography.label,
                        color = if (selected) colors.primaryInk else colors.muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * Estructura de las 4 pestañas: barra superior con el selector de mascota (y acciones a la derecha),
 * contenido y barra inferior.
 */
@Composable
fun MainScaffold(
    tabs: List<BottomBarItem>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    petSelector: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = BestIdTheme.colors
    Scaffold(
        modifier = modifier,
        containerColor = colors.bg,
        contentColor = colors.ink,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bg)
                    .statusBarsPadding()
                    .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) { petSelector() }
                actions()
            }
        },
        bottomBar = { BottomBar(items = tabs, selectedIndex = selectedTab, onSelect = onSelectTab) },
        snackbarHost = { SnackbarHost(snackbarHostState) { UndoSnackbar(it) } },
        content = content,
    )
}

/** Botón de ícono con alto táctil de 48 y descripción obligatoria. */
@Composable
fun IconAction(icon: DrawableResource, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(BestIdTheme.sizes.minTouch)) {
        Icon(painterResource(icon), contentDescription = contentDescription, tint = BestIdTheme.colors.ink)
    }
}

/**
 * Estructura de flujos modales y formularios: título, botón para cerrar, contenido desplazable
 * y acción principal fija al pie (sube con el teclado).
 */
@Composable
fun FlowScaffold(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    closeIcon: DrawableResource = BestIdIcons.Close,
    closeDescription: String? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BestIdTheme.colors
    Scaffold(
        modifier = modifier,
        containerColor = colors.bg,
        contentColor = colors.ink,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = BestIdTheme.spacing.xs, vertical = BestIdTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconAction(closeIcon, closeDescription ?: stringResource(Res.string.ds_close), onClose)
                Text(
                    title,
                    style = BestIdTheme.typography.subtitle,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
            }
        },
        bottomBar = {
            if (bottomAction != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bg)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.sm),
                ) { bottomAction() }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { UndoSnackbar(it) } },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
            content = content,
        )
    }
}

private val previewTabs = PreviewData.TABS.zip(
    listOf(
        BestIdIcons.Home to BestIdIcons.HomeFilled,
        BestIdIcons.Vaccines to BestIdIcons.VaccinesFilled,
        BestIdIcons.Health to BestIdIcons.HealthFilled,
        BestIdIcons.Pets to BestIdIcons.PetsFilled,
    ),
) { label, (icon, selected) -> BottomBarItem(label, icon, selected) }

@Preview
@Composable
private fun MainScaffoldPreview() = AppTheme {
    MainScaffold(
        tabs = previewTabs,
        selectedTab = 1,
        onSelectTab = {},
        petSelector = {
            PetSelector(listOf(PetSelectorItem("1", PreviewData.PET_NAME, null, AvatarSpecies.CAT)), "1", {}, {})
        },
        actions = { IconAction(BestIdIcons.Settings, PreviewData.FLOW_TITLE, onClick = {}) },
    ) { padding ->
        EmptyState(
            BestIdIcons.Vaccines,
            PreviewData.EMPTY_TITLE,
            PreviewData.EMPTY_BODY,
            modifier = Modifier.padding(padding),
        )
    }
}

@Preview(fontScale = 2f)
@Composable
private fun BottomBarLongTextPreview() = AppTheme {
    BottomBar(items = previewTabs, selectedIndex = 0, onSelect = {})
}

@Preview
@Composable
private fun FlowScaffoldPreview() = AppTheme {
    FlowScaffold(
        title = PreviewData.FLOW_TITLE,
        onClose = {},
        bottomAction = { PrimaryButton(PreviewData.BUTTON, onClick = {}) },
    ) {
        FormField(PreviewData.FIELD_LABEL, PreviewData.FIELD_VALUE, {})
        DateField(PreviewData.DATE_LABEL, null, onClick = {})
    }
}
