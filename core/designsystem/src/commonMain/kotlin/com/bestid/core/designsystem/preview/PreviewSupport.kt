package com.bestid.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.theme.AppTheme
import com.bestid.core.designsystem.theme.BestIdTheme

/** Envoltorio de las previews: tema, fondo de pantalla y margen lateral. */
@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    AppTheme {
        Column(
            modifier = Modifier
                .background(BestIdTheme.colors.bg)
                .padding(BestIdTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
        ) { content() }
    }
}

/**
 * Datos de ejemplo para previews y para el catálogo de componentes (solo debug).
 * No es texto de la app: las pantallas reales leen sus textos de Compose Resources.
 */
object PreviewData {
    const val PET_NAME = "Lilith"
    const val PET_NAME_2 = "Toby"
    const val ADD_PAGE = "Otra página"
    const val LONG_TEXT =
        "Texto muy largo para comprobar que el componente crece y no corta contenido con letra grande"
    const val BUTTON = "Guardar 2 vacunas"
    const val BUTTON_SECONDARY = "Elegir de galería"
    const val BUTTON_DASHED = "Agregar otra mascota"
    const val TEXT_ACTION = "Ver todas"
    const val FIELD_LABEL = "Nombre"
    const val FIELD_VALUE = "Lilith"
    const val FIELD_PLACEHOLDER = "Ej. Lilith"
    const val FIELD_HELPER = "Como aparece en la cartilla"
    const val FIELD_ERROR = "Escribe un nombre de 1 a 40 caracteres"
    const val DATE_LABEL = "Fecha de aplicación"
    const val DATE_VALUE = "21/12/2025"
    const val SPECIES_LABEL = "Especie"
    val SPECIES = listOf("Gato", "Perro", "Otra")
    val VACCINE_TYPES = listOf("Calicivirus", "Rinotraqueitis", "Panleucopenia", "Rabia")
    val SEX = listOf("Hembra", "Macho")
    val HEALTH_TABS = listOf("Cuidados", "Peso")
    val TABS = listOf("Inicio", "Cartilla", "Salud", "Mascota")
    const val SWITCH_TITLE = "Avisarme"
    const val SWITCH_SUBTITLE = "7 días antes, 1 día antes y el mismo día"
    const val INFO = "Lilith y sus 2 vacunas guardadas"
    const val ALERT = "La próxima fecha no se lee bien. Complétala para recibir el aviso."
    const val STAT_LABEL = "Último peso"
    const val STAT_VALUE = "4,2 kg"
    const val STAT_ACTION = "Agregar"
    const val ROW_LABEL = "Especie"
    const val ROW_VALUE = "Gato"
    const val BADGE_OK = "Al día"
    const val BADGE_ALERT = "En 5 días"
    const val BADGE_NEUTRAL = "Escaneada"
    const val RECORD_DATE = "21/12/2025"
    const val RECORD_PRODUCTS = "Felocell 3 · lote 774233A"
    const val RECORD_VET = "Veterinaria San Martín"
    const val REVIEW_REASON = "Último dígito del año tapado por un sello"
    const val REVIEW_QUESTION = "¿Le pusieron la antirrábica?"
    val REVIEW_ANSWERS = listOf("Sí, se la pusieron", "No estoy seguro")
    const val REVIEW_TITLE = "21/12/2025 · 3 vacunas"
    const val NOTIFICATION_TITLE = "Vacuna de Lilith"
    const val NOTIFICATION_BODY = "A Lilith le toca su vacuna de leucemia en 7 días"
    const val EMPTY_TITLE = "Aún no hay vacunas"
    const val EMPTY_BODY = "Escanea la cartilla o agrega una vacuna a mano."
    const val EMPTY_ACTION = "Agregar vacuna"
    const val ERROR = "No pudimos leer la cartilla"
    const val PERMISSION_TITLE = "Los avisos están desactivados"
    const val PERMISSION_BODY = "Actívalos en los ajustes del teléfono para saber cuándo toca la próxima vacuna."
    const val CONFIRM_TITLE = "¿Eliminar esta vacuna?"
    const val CONFIRM_BODY = "Podrás deshacerlo durante unos segundos."
    const val CONFIRM_ACTION = "Eliminar"
    const val UNDO_MESSAGE = "Vacuna eliminada"
    const val FLOW_TITLE = "Nueva vacuna"
    const val ID_SEX = "Sexo"
    const val RECORD_PHOTOS = "1 foto de la cartilla"
    const val ID_BIRTH = "Nacimiento"
    const val ID_OWNER = "Tutor"
    const val ID_OWNER_NAME = "Tutor de Prueba"
    const val ID_SUBTITLE = "Gata · Hembra"
    const val ID_VACCINES = "2 vacunas"
    const val ID_NEXT = "31 dic"
    const val ID_MISSING = "Faltan raza, nacimiento y señas"
    const val ID_COMPLETE = "Completar"
    val WEIGHT_POINTS = listOf("jul" to 3.6, "ago" to 3.9, "set" to 4.0, "oct" to 4.1, "nov" to 4.2)
}
