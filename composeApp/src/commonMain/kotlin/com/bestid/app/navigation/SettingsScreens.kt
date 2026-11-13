package com.bestid.app.navigation

import androidx.compose.runtime.Composable
import com.bestid.app.resources.Res
import com.bestid.app.resources.back
import com.bestid.app.resources.catalog_title
import com.bestid.app.resources.settings
import com.bestid.app.resources.settings_coming_body
import com.bestid.app.resources.settings_coming_title
import com.bestid.app.resources.settings_open_catalog
import com.bestid.core.designsystem.catalog.ComponentCatalog
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.icon.BestIdIcons
import org.jetbrains.compose.resources.stringResource

/** Ajustes llega en M4. En M0 sirve de acceso al catálogo de componentes para revisar en teléfono. */
@Composable
internal fun SettingsPlaceholder(onClose: () -> Unit, onOpenCatalog: () -> Unit) {
    FlowScaffold(
        title = stringResource(Res.string.settings),
        onClose = onClose,
        closeIcon = BestIdIcons.ArrowBack,
        closeDescription = stringResource(Res.string.back),
    ) {
        EmptyState(
            illustration = BestIdIcons.Settings,
            title = stringResource(Res.string.settings_coming_title),
            body = stringResource(Res.string.settings_coming_body),
        )
        SecondaryButton(text = stringResource(Res.string.settings_open_catalog), onClick = onOpenCatalog)
    }
}

@Composable
internal fun CatalogScreen(onClose: () -> Unit) {
    ComponentCatalog(title = stringResource(Res.string.catalog_title), onClose = onClose)
}
