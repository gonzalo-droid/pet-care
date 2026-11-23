package com.bestid.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.bestid.core.designsystem.component.BrandLogo
import com.bestid.core.designsystem.component.FillScrollColumn
import com.bestid.core.designsystem.component.NotificationPreview
import com.bestid.core.designsystem.component.PageIndicator
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.feature.onboarding.resources.Res
import com.bestid.feature.onboarding.resources.welcome_body
import com.bestid.feature.onboarding.resources.welcome_example_body
import com.bestid.feature.onboarding.resources.welcome_example_title
import com.bestid.feature.onboarding.resources.welcome_history_body
import com.bestid.feature.onboarding.resources.welcome_history_title
import com.bestid.feature.onboarding.resources.welcome_next
import com.bestid.feature.onboarding.resources.welcome_page
import com.bestid.feature.onboarding.resources.welcome_privacy
import com.bestid.feature.onboarding.resources.welcome_reminders_body
import com.bestid.feature.onboarding.resources.welcome_reminders_title
import com.bestid.feature.onboarding.resources.welcome_restore
import com.bestid.feature.onboarding.resources.welcome_scan
import com.bestid.feature.onboarding.resources.welcome_skip
import com.bestid.feature.onboarding.resources.welcome_title
import com.bestid.feature.onboarding.resources.welcome_without_card
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val PAGES = 3

/**
 * Bienvenida (O1), cuando no hay mascotas: tres páginas que cuentan qué es la app (D46). Las dos
 * primeras tienen "Siguiente" y "Saltar"; la última, las acciones de siempre. "Empezar con su
 * cartilla" pide primero la mascota y sigue a las fotos; la lectura automática llega después (D29).
 */
@Composable
fun WelcomeScreen(onStartWithCard: () -> Unit, onStartWithoutCard: () -> Unit, onRestore: () -> Unit) {
    val pager = rememberPagerState { PAGES }
    val scope = rememberCoroutineScope()
    fun goTo(page: Int) = scope.launch { pager.animateScrollToPage(page) }

    HorizontalPager(
        state = pager,
        modifier = Modifier.fillMaxSize().background(BestIdTheme.colors.bg),
    ) { page ->
        WelcomePage(page) {
            PageIndicator(
                count = PAGES,
                current = page,
                description = stringResource(Res.string.welcome_page, page + 1, PAGES),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = BestIdTheme.spacing.xs),
            )
            if (page < PAGES - 1) {
                PrimaryButton(stringResource(Res.string.welcome_next), onClick = { goTo(page + 1) })
                TextAction(stringResource(Res.string.welcome_skip), onClick = { goTo(PAGES - 1) }, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                PrimaryButton(stringResource(Res.string.welcome_scan), onClick = onStartWithCard, icon = BestIdIcons.PhotoCamera)
                SecondaryButton(stringResource(Res.string.welcome_without_card), onClick = onStartWithoutCard)
                // Teléfono nuevo: importar el respaldo del anterior (sección 13).
                TextAction(stringResource(Res.string.welcome_restore), onClick = onRestore, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    stringResource(Res.string.welcome_privacy),
                    style = BestIdTheme.typography.support,
                    color = BestIdTheme.colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(page: Int, bottom: @Composable ColumnScope.() -> Unit) {
    val (title, body) = when (page) {
        0 -> Res.string.welcome_title to Res.string.welcome_body
        1 -> Res.string.welcome_history_title to Res.string.welcome_history_body
        else -> Res.string.welcome_reminders_title to Res.string.welcome_reminders_body
    }
    FillScrollColumn(
        bottom = bottom,
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.lg),
    ) {
        BrandLogo()
        when (page) {
            0 -> PetsArt()
            1 -> HistoryArt()
            else -> ReminderArt()
        }
        PageText(title, body)
    }
}

@Composable
private fun PageText(title: StringResource, body: StringResource) {
    Text(stringResource(title), style = BestIdTheme.typography.hero, modifier = Modifier.semantics { heading() })
    Text(stringResource(body), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
}

/** Gato y perro sobre el fondo verde suave. */
@Composable
private fun PetsArt() = ArtBox {
    ArtRow {
        Image(painterResource(BestIdIcons.CatIllustration), contentDescription = null, modifier = Modifier.size(BestIdTheme.sizes.illustration))
        Image(painterResource(BestIdIcons.DogIllustration), contentDescription = null, modifier = Modifier.size(BestIdTheme.sizes.illustration))
    }
}

/** Vacunas, cuidados y peso: lo que guarda la app. */
@Composable
private fun HistoryArt() = ArtBox {
    ArtRow { listOf(BestIdIcons.Vaccines, BestIdIcons.Health, BestIdIcons.Weight).forEach { ArtIcon(it) } }
}

/** Un aviso de ejemplo, como llega al teléfono. */
@Composable
private fun ReminderArt() = ArtBox {
    NotificationPreview(
        title = stringResource(Res.string.welcome_example_title),
        body = stringResource(Res.string.welcome_example_body),
        modifier = Modifier.fillMaxWidth().padding(horizontal = BestIdTheme.spacing.md),
    )
}

/** Mismo alto mínimo en las tres páginas, para que el título no salte al deslizar. */
@Composable
private fun ArtBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BestIdTheme.colors.primarySoft, BestIdTheme.shapes.cardLarge)
            .padding(vertical = BestIdTheme.spacing.xl)
            .heightIn(min = BestIdTheme.sizes.illustration),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun ArtRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

@Composable
private fun ArtIcon(icon: DrawableResource) {
    Box(
        Modifier.size(BestIdTheme.sizes.introIconCircle).background(BestIdTheme.colors.surface, BestIdTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = BestIdTheme.colors.primary, modifier = Modifier.size(BestIdTheme.sizes.introIcon))
    }
}
