package com.bestid.core.ui

import androidx.compose.runtime.Composable
import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.ui.resources.Res
import com.bestid.core.ui.resources.care_deworm_external
import com.bestid.core.ui.resources.care_deworm_internal
import com.bestid.core.ui.resources.care_other
import com.bestid.core.ui.resources.weight_kg
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun CareKind.label(): StringResource = when (this) {
    CareKind.DEWORM_INTERNAL -> Res.string.care_deworm_internal
    CareKind.DEWORM_EXTERNAL -> Res.string.care_deworm_external
    CareKind.OTHER -> Res.string.care_other
}

/** Nombre de un cuidado: su título si es "Otro", si no el tipo. */
@Composable
fun CareTask.displayName(): String = title?.takeIf { kind == CareKind.OTHER } ?: stringResource(kind.label())

/** Número con coma decimal y hasta 2 decimales: 3800 g → "3,8"; 4250 → "4,25"; 50 → "0,05". */
fun formatKgNumber(grams: Int): String {
    val hundredths = (grams + 5) / 10
    val whole = hundredths / 100
    val decimals = (hundredths % 100).toString().padStart(2, '0').trimEnd('0')
    return if (decimals.isEmpty()) "$whole" else "$whole,$decimals"
}

/** "3,8 kg" */
@Composable
fun formatWeight(grams: Int): String = stringResource(Res.string.weight_kg, formatKgNumber(grams))
