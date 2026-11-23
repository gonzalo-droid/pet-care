package com.bestid.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bestid.app.di.appModules
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.domain.reminder.PlannedReminder
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Accesibilidad (regla 2, sección 15) con el texto al 200 %: en cada pantalla principal ningún texto
 * se corta y todo lo que se toca mide al menos 48 × 48 dp.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalForeignApi::class)
class AccessibilityUiTest {
    private val dir = NSTemporaryDirectory() + NSUUID().UUIDString

    @BeforeTest
    fun startApp() {
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        startKoin {
            allowOverride(true)
            modules(
                appModules + module {
                    single<RoomDatabase.Builder<BestIdDatabase>> { Room.inMemoryDatabaseBuilder<BestIdDatabase>() }
                    single { PreferenceDataStoreFactory.createWithPath { "$dir/prefs.preferences_pb".toPath() } }
                    single<NotificationScheduler> {
                        object : NotificationScheduler {
                            override suspend fun permission() = NotificationPermission.DENIED
                            override suspend fun schedule(reminders: List<PlannedReminder>) = Unit
                            override suspend fun cancel(ids: List<String>) = Unit
                        }
                    }
                },
            )
        }
    }

    @AfterTest
    fun stop() = stopKoin()

    @Test
    fun largeTextDoesNotCutAndTouchTargetsAreBigEnough() = runComposeUiTest {
        setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) { App() }
        }

        waitForText("La cartilla de tu mascota")
        checkScreen("Bienvenida")

        onNodeWithText("Empezar sin cartilla").performScrollTo().performClick()
        waitForText("Nueva mascota")
        checkScreen("Nueva mascota")
        onNode(hasSetTextAction() and hasContentDescription("Nombre")).performTextInput("Lilith")
        onNodeWithText("Gato").performScrollTo().performClick()
        onNodeWithText("Guardar mascota").performClick()

        waitForText("Inicio")
        checkScreen("Inicio")
        for (tab in listOf("Cartilla", "Salud", "Mascota")) {
            onNodeWithText(tab).performClick()
            waitForIdle()
            checkScreen(tab)
        }

        onNode(hasContentDescription("Ajustes")).performClick()
        waitForText("Hora de los avisos")
        checkScreen("Ajustes")
        onNodeWithText("Aún no exportaste un respaldo").performScrollTo().performClick()
        waitForText("Exportar respaldo")
        checkScreen("Respaldo")
    }

    private fun ComposeUiTest.waitForText(text: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * Corte real: se sale en alto, alguna línea lleva elipsis o el texto sin salto de línea es más ancho que su caja.
     * No basta `hasVisualOverflow`: con `TextAlign.Center` y ancho ajustado al contenido (los botones),
     * el párrafo se arma al ancho máximo y queda "más ancho" que el texto aunque se vea completo.
     */
    private fun TextLayoutResult.isCut(): Boolean =
        didOverflowHeight ||
            (0 until lineCount).any { isLineEllipsized(it) } ||
            (!layoutInput.softWrap && multiParagraph.intrinsics.maxIntrinsicWidth > size.width)

    private fun ComposeUiTest.checkScreen(screen: String) {
        waitForIdle()
        val problems = mutableListOf<String>()

        // Textos cortados: con elipsis o recortados por falta de espacio.
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)).fetchSemanticsNodes().forEach { node ->
            val layouts = mutableListOf<TextLayoutResult>()
            node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
            layouts.filter { it.isCut() }.forEach {
                problems += "texto cortado: \"${it.layoutInput.text}\""
            }
        }

        // Toques de 48 dp o más (cuenta el área táctil ampliada, no solo lo que se dibuja).
        onAllNodes(hasClickAction()).fetchSemanticsNodes().forEach { node ->
            // Un elemento a medio salir de la pantalla se ve recortado: vale su tamaño real.
            val bounds = node.touchBoundsInRoot
            val width = maxOf(bounds.width, node.size.width.toFloat())
            val height = maxOf(bounds.height, node.size.height.toFloat())
            val minPx = 48 * density.density - 1
            if (width < minPx || height < minPx) {
                val label = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                    ?: node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
                    ?: "sin nombre"
                problems += "toque chico (${(width / density.density).toInt()}×${(height / density.density).toInt()} dp): $label"
            }
        }
        check(problems.isEmpty()) { "$screen:\n" + problems.joinToString("\n") }
    }
}
