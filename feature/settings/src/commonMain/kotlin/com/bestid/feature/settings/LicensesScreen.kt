package com.bestid.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.feature.settings.resources.Res
import com.bestid.feature.settings.resources.licenses_apache_body
import com.bestid.feature.settings.resources.licenses_apache_title
import com.bestid.feature.settings.resources.licenses_hide_text
import com.bestid.feature.settings.resources.licenses_intro
import com.bestid.feature.settings.resources.licenses_ofl_body
import com.bestid.feature.settings.resources.licenses_ofl_title
import com.bestid.feature.settings.resources.licenses_show_text
import com.bestid.feature.settings.resources.licenses_sqlite
import com.bestid.feature.settings.resources.licenses_title
import com.bestid.feature.settings.resources.settings_back
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Licencias (AJU-01): qué se usa, bajo qué licencia, y el texto completo a pedido. */
@Composable
fun LicensesScreen(onClose: () -> Unit) {
    FlowScaffold(
        title = stringResource(Res.string.licenses_title),
        onClose = onClose,
        closeIcon = BestIdIcons.ArrowBack,
        closeDescription = stringResource(Res.string.settings_back),
    ) {
        Text(stringResource(Res.string.licenses_intro), style = BestIdTheme.typography.body)
        LicenseCard(Res.string.licenses_apache_title, Res.string.licenses_apache_body, "files/licenses/Apache-2.0.txt")
        LicenseCard(Res.string.licenses_ofl_title, Res.string.licenses_ofl_body, "files/licenses/Nunito-OFL.txt")
        Text(stringResource(Res.string.licenses_sqlite), style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
    }
}

@Composable
private fun LicenseCard(title: StringResource, body: StringResource, file: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    PlainCard {
        Eyebrow(stringResource(title))
        Text(stringResource(body), style = BestIdTheme.typography.body)
        TextAction(
            text = stringResource(if (expanded) Res.string.licenses_hide_text else Res.string.licenses_show_text),
            onClick = { expanded = !expanded },
        )
        AnimatedVisibility(visible = expanded) {
            val text by produceState("", file) { value = Res.readBytes(file).decodeToString() }
            Text(text, style = BestIdTheme.typography.support)
        }
    }
}
