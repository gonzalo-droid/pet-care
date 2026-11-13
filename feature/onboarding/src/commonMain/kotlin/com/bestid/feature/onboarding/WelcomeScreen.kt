package com.bestid.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.bestid.core.designsystem.component.BrandLogo
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.feature.onboarding.resources.Res
import com.bestid.feature.onboarding.resources.welcome_body
import com.bestid.feature.onboarding.resources.welcome_privacy
import com.bestid.feature.onboarding.resources.welcome_scan
import com.bestid.feature.onboarding.resources.welcome_title
import com.bestid.feature.onboarding.resources.welcome_without_card
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Bienvenida (O1), cuando no hay mascotas. "Empezar con su cartilla" pide primero la mascota (nombre
 * y especie) y sigue a las fotos de la cartilla; la lectura automática llega después (D29).
 */
@Composable
fun WelcomeScreen(onStartWithCard: () -> Unit, onStartWithoutCard: () -> Unit) {
    val colors = BestIdTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
    ) {
        BrandLogo()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primarySoft, BestIdTheme.shapes.cardLarge)
                .padding(vertical = BestIdTheme.spacing.xl),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(BestIdIcons.CatIllustration), contentDescription = null, modifier = Modifier.size(BestIdTheme.sizes.illustration))
            Image(painterResource(BestIdIcons.DogIllustration), contentDescription = null, modifier = Modifier.size(BestIdTheme.sizes.illustration))
        }
        Text(stringResource(Res.string.welcome_title), style = BestIdTheme.typography.hero, modifier = Modifier.semantics { heading() })
        Text(stringResource(Res.string.welcome_body), style = BestIdTheme.typography.body, color = colors.muted)
        Spacer(Modifier.weight(1f, fill = false))
        PrimaryButton(stringResource(Res.string.welcome_scan), onClick = onStartWithCard, icon = BestIdIcons.PhotoCamera)
        SecondaryButton(stringResource(Res.string.welcome_without_card), onClick = onStartWithoutCard)
        Text(
            stringResource(Res.string.welcome_privacy),
            style = BestIdTheme.typography.support,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
