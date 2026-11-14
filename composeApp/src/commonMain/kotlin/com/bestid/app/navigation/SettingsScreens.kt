package com.bestid.app.navigation

import androidx.compose.runtime.Composable
import com.bestid.app.resources.Res
import com.bestid.app.resources.catalog_title
import com.bestid.core.designsystem.catalog.ComponentCatalog
import org.jetbrains.compose.resources.stringResource

/** Catálogo de componentes (herramienta de desarrollo, se abre desde Ajustes). */
@Composable
internal fun CatalogScreen(onClose: () -> Unit) {
    ComponentCatalog(title = stringResource(Res.string.catalog_title), onClose = onClose)
}
