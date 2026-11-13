package com.bestid.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.nunito_bold
import com.bestid.core.designsystem.resources.nunito_extrabold
import com.bestid.core.designsystem.resources.nunito_medium
import org.jetbrains.compose.resources.Font

/**
 * Escala tipográfica de la sección 7. Todo en `sp` para respetar la escala de texto del sistema.
 * Ningún estilo baja de 13 sp.
 */
@Immutable
data class BestIdTypography(
    /** 32/800 */
    val display: TextStyle,
    /** 26/800 */
    val title: TextStyle,
    /** 20/800 */
    val subtitle: TextStyle,
    /** 18/800 */
    val card: TextStyle,
    /** 16/700 */
    val body: TextStyle,
    /** 16/800: etiquetas de campo, opciones y botones secundarios */
    val bodyStrong: TextStyle,
    /** 17/700, texto que escribe la persona en un campo (prototipo) */
    val input: TextStyle,
    /** 18/800, botón principal */
    val button: TextStyle,
    /** 14/700 */
    val support: TextStyle,
    /** 14/800, chips y badges */
    val chip: TextStyle,
    /** 13/700, etiquetas de la barra inferior */
    val label: TextStyle,
)

@Composable
internal fun nunitoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.nunito_medium, FontWeight.Medium),
    Font(Res.font.nunito_bold, FontWeight.Bold),
    Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
)

internal fun bestIdTypography(family: FontFamily): BestIdTypography {
    fun style(size: Int, weight: FontWeight, lineHeight: Int) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
    )
    return BestIdTypography(
        display = style(32, FontWeight.ExtraBold, 40),
        title = style(26, FontWeight.ExtraBold, 32),
        subtitle = style(20, FontWeight.ExtraBold, 26),
        card = style(18, FontWeight.ExtraBold, 24),
        body = style(16, FontWeight.Bold, 22),
        bodyStrong = style(16, FontWeight.ExtraBold, 22),
        input = style(17, FontWeight.Bold, 24),
        button = style(18, FontWeight.ExtraBold, 24),
        support = style(14, FontWeight.Bold, 20),
        chip = style(14, FontWeight.ExtraBold, 20),
        label = style(13, FontWeight.Bold, 18),
    )
}

internal fun BestIdTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = display,
    displayMedium = display,
    displaySmall = display,
    headlineLarge = title,
    headlineMedium = title,
    headlineSmall = subtitle,
    titleLarge = subtitle,
    titleMedium = card,
    titleSmall = body,
    bodyLarge = body,
    bodyMedium = support,
    bodySmall = support,
    labelLarge = body,
    labelMedium = support,
    labelSmall = label,
)
