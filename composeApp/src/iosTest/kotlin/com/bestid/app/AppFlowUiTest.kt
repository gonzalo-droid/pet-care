package com.bestid.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bestid.app.di.appModules
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.domain.reminder.PlannedReminder
import com.bestid.core.domain.reminder.ReminderSubject
import com.bestid.core.platform.date.DatePickerOverride
import com.bestid.core.platform.date.LocalDatePickerOverride
import com.bestid.core.platform.image.ImagePickerOverride
import com.bestid.core.platform.image.ImagePickerResult
import com.bestid.core.platform.image.LocalImagePickerOverride
import com.bestid.core.platform.share.FilePickerOverride
import com.bestid.core.platform.share.LocalFilePickerOverride
import com.bestid.core.platform.share.LocalShareSheetOverride
import com.bestid.core.platform.share.ShareSheet
import com.bestid.core.data.repository.OkioPhotoFileStore
import com.bestid.core.domain.port.PhotoFileStore
import okio.FileSystem
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.datetime.LocalDate
import okio.Path.Companion.toPath
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Flujos principales en el simulador de iOS, tocando la app real (App() con Room y navegación).
 * M1: registrar a Lilith con sus 2 vacunas a mano, agregar a Toby y cambiar entre las dos.
 * M2: agregar un cuidado con aviso (pantalla de permiso y avisos programados) y un peso.
 * M3: primer uso desde la Bienvenida con foto de Lilith y 2 páginas de la cartilla, que quedan en el
 * detalle de la vacuna y se abren en el visor.
 * M4: compartir la ficha en PDF, exportar el respaldo, borrar todo e importarlo desde la Bienvenida.
 * El selector de fecha nativo y la galería se reemplazan (no se pueden tocar desde Compose); las fotos
 * falsas pasan por el ImageProcessor real de iOS.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalForeignApi::class)
class AppFlowUiTest {
    private val dates = ArrayDeque<LocalDate>()
    private val dir = NSTemporaryDirectory() + NSUUID().UUIDString

    /** Hoja de compartir falsa: guarda la ruta de cada archivo que la app quiso compartir. */
    private val shared = mutableListOf<String>()
    private val shareSheet = object : ShareSheet {
        override fun share(path: String, mimeType: String, title: String) {
            shared += path
        }
    }

    /** Galería falsa: devuelve fotos JPEG generadas, tantas como permita el selector (máximo 2). */
    private val gallery = ImagePickerOverride { _, max -> ImagePickerResult.Picked(List(minOf(max, 2)) { samplePhoto(it) }) }

    /** UNUserNotificationCenter no funciona fuera de una app instalada: se usa un programador falso. */
    private val scheduler = object : NotificationScheduler {
        val scheduled = mutableMapOf<String, PlannedReminder>()
        override suspend fun permission() = NotificationPermission.NOT_DETERMINED
        override suspend fun schedule(reminders: List<PlannedReminder>) = reminders.forEach { scheduled[it.id] = it }
        override suspend fun cancel(ids: List<String>) = ids.forEach { scheduled.remove(it) }
    }

    @BeforeTest
    fun startApp() {
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        startKoin {
            allowOverride(true)
            modules(
                appModules + module {
                    single<RoomDatabase.Builder<BestIdDatabase>> { Room.inMemoryDatabaseBuilder<BestIdDatabase>() }
                    single { PreferenceDataStoreFactory.createWithPath { "$dir/prefs.preferences_pb".toPath() } }
                    single<NotificationScheduler> { scheduler }
                    single<PhotoFileStore> { OkioPhotoFileStore(dir, FileSystem.SYSTEM) }
                },
            )
        }
    }

    @AfterTest
    fun stop() = stopKoin()

    @Test
    fun petsVaccinesCaresAndWeight() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(
                LocalDatePickerOverride provides DatePickerOverride { dates.removeFirstOrNull() },
                LocalImagePickerOverride provides gallery,
                LocalShareSheetOverride provides shareSheet,
                LocalFilePickerOverride provides FilePickerOverride { shared.lastOrNull { it.endsWith(".zip") } },
            ) {
                App()
            }
        }

        // Sin mascotas: Bienvenida (O1)
        waitForText("La cartilla de tu mascota")
        screenshot("01-bienvenida")
        onNodeWithText("Empezar con su cartilla").performClick()

        // Lilith, con foto
        waitForText("Nueva mascota")
        onNodeWithText("Agregar foto").performClick()
        waitForText("Foto de tu mascota")
        onNodeWithText("Elegir de galería").performClick()
        waitForText("Cambiar foto")
        onNode(hasSetTextAction() and hasContentDescription("Nombre")).performTextInput("Lilith")
        onNodeWithText("Gato").performScrollTo().performClick()
        screenshot("02-mascota-con-foto")
        onNodeWithText("Guardar mascota").performClick()

        // Fotos de la cartilla (O2): 2 páginas de la galería
        waitForText("Una foto por cada página")
        onNodeWithText("Galería").performClick()
        waitForText("Pág. 2")
        screenshot("03-fotos-cartilla")
        onNodeWithText("Continuar con 2 fotos").performClick()

        // Vacuna 1 (29/06/2024, triple felina) llenada viendo las fotos
        waitForText("Toca una foto para verla en grande")
        onNode(hasContentDescription("Página 2 de la cartilla")).assertExistsSafely()
        fillVaccine(LocalDate(2024, 6, 29), listOf("Calicivirus", "Rinotraqueítis", "Panleucopenia"))
        waitForText("Agregar vacuna")
        // Vacuna 2: 21/12/2025, triple felina + rabia, con su frasco
        addVaccine(LocalDate(2025, 12, 21), listOf("Calicivirus", "Rinotraqueítis", "Panleucopenia", "Rabia"))
        waitForText("2 vacunas registradas")
        screenshot("02-inicio-lilith")

        // Cartilla
        onNodeWithText("Cartilla").performClick()
        waitForText("21 dic 2025")
        onNodeWithText("29 jun 2024").assertExistsSafely()
        screenshot("03-cartilla-lilith")

        // Las páginas quedan en el detalle de la vacuna y se abren en el visor
        onNodeWithText("29 jun 2024").performClick()
        waitForText("FOTOS")
        screenshot("03b-detalle-con-fotos")
        onNode(hasContentDescription("Página 1 de la cartilla")).performScrollTo().performClick()
        waitUntil(timeoutMillis = 10_000) {
            onAllNodes(hasContentDescription("Foto ampliada", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
        screenshot("03c-visor")
        onAllNodes(hasContentDescription("Cerrar"))[0].performClick()
        onNode(hasContentDescription("Volver", substring = true)).performClick()
        waitForText("21 dic 2025")
        // 1 foto de la mascota + 2 páginas, procesadas y guardadas en el directorio de la app
        check(NSFileManager.defaultManager.contentsOfDirectoryAtPath("$dir/photos", null).orEmpty().size == 3)

        // Segunda mascota desde el selector
        onNode(hasContentDescription("Cambiar de mascota. Seleccionada: Lilith")).performClick()
        waitForText("Tus mascotas")
        onAllNodesWithText("Agregar mascota")[0].performClick()
        fillPetForm(name = "Toby", species = "Perro")
        waitForText("Aún no hay vacunas")
        screenshot("04-cartilla-toby")

        // Volver a Lilith
        onNode(hasContentDescription("Cambiar de mascota. Seleccionada: Toby")).performClick()
        waitForText("Tus mascotas")
        onAllNodesWithText("Lilith")[0].performClick()
        waitForText("21 dic 2025")
        screenshot("05-cartilla-lilith-de-nuevo")

        // Carnet
        onNodeWithText("Mascota").performClick()
        waitForText("Completar datos")
        screenshot("06-carnet-lilith")

        // M2: un cuidado con aviso (primer logro → pantalla de permiso) y un peso
        onNodeWithText("Salud").performClick()
        waitForText("Aún no hay cuidados")
        onNodeWithText("Agregar cuidado").performScrollTo().performClick()
        waitForText("Nuevo cuidado")
        dates.addLast(LocalDate(2026, 10, 1))
        onNode(hasContentDescription("Última vez", substring = true)).performScrollTo().performClick()
        onNode(hasSetTextAction() and hasContentDescription("Cada cuánto")).performTextInput("3")
        screenshot("07-nuevo-cuidado")
        onNodeWithText("Guardar cuidado").performClick()
        waitForText("¿Te avisamos cuando le toque?")
        screenshot("08-permiso-avisos")
        onNodeWithText("Ahora no").performClick()
        waitForText("Desparasitación interna")
        waitUntil(timeoutMillis = 10_000) { scheduler.scheduled.values.any { it.subject is ReminderSubject.Care } }
        screenshot("09-salud-cuidados")

        onNodeWithText("Peso").performClick()
        onNodeWithText("Agregar peso").performScrollTo().performClick()
        waitForText("Guardar peso")
        onNode(hasSetTextAction() and hasContentDescription("Peso en kg")).performTextInput("3,8")
        onNodeWithText("Guardar peso").performClick()
        waitForText("3,8 kg")
        screenshot("10-salud-peso")

        // M4: ficha en PDF desde el carnet
        onNodeWithText("Mascota").performClick()
        onNodeWithText("Compartir ficha").performScrollTo().performClick()
        waitForText("Compartir PDF")
        screenshot("11-compartir-ficha")
        onNodeWithText("Compartir PDF").performClick()
        waitUntil(timeoutMillis = 20_000) { shared.any { it.endsWith(".pdf") } }
        val pdf = shared.last { it.endsWith(".pdf") }
        check(readStart(pdf, 5) == "%PDF-") { "No es un PDF: $pdf" }
        onAllNodes(hasContentDescription("Cerrar"))[0].performClick()

        // Respaldo: exportar
        onNode(hasContentDescription("Ajustes")).performClick()
        waitForText("Hora de los avisos")
        screenshot("12-ajustes")
        onNodeWithText("Aún no exportaste un respaldo").performClick()
        onNodeWithText("Exportar respaldo").performClick()
        waitUntil(timeoutMillis = 20_000) { shared.any { it.endsWith(".zip") } }
        check(readStart(shared.last { it.endsWith(".zip") }, 2) == "PK") { "No es un zip" }
        waitForText("Último respaldo exportado")
        screenshot("13-respaldo")
        onNode(hasContentDescription("Volver")).performClick()

        // Borrar todo: vuelve a la Bienvenida
        waitForText("Borrar todos los datos")
        onNodeWithText("Borrar todos los datos").performScrollTo().performClick()
        onNodeWithText("Borrar todo").performClick()
        waitForText("La cartilla de tu mascota")
        check(NSFileManager.defaultManager.contentsOfDirectoryAtPath("$dir/photos", null).orEmpty().isEmpty())

        // Teléfono "nuevo": importar el respaldo desde la Bienvenida
        onNodeWithText("Tengo un respaldo").performClick()
        onNodeWithText("Importar respaldo").performClick()
        waitForText("Respaldo importado")
        waitForText("2 mascotas")
        screenshot("14-respaldo-importado")
        onNode(hasContentDescription("Volver")).performClick()
        waitForText("Lilith")
        check(NSFileManager.defaultManager.contentsOfDirectoryAtPath("$dir/photos", null).orEmpty().size == 3)
    }

    private fun ComposeUiTest.fillPetForm(name: String, species: String) {
        waitForText("Nueva mascota")
        onNode(hasSetTextAction() and hasContentDescription("Nombre")).performTextInput(name)
        onNodeWithText(species).performScrollTo().performClick()
        onNodeWithText("Guardar mascota").performClick()
    }

    /** "Agregar vacuna" abre las fotos de la cartilla; se sigue a mano. */
    private fun ComposeUiTest.addVaccine(date: LocalDate, types: List<String>) {
        onNodeWithText("Agregar vacuna").performScrollTo().performClick()
        waitForText("Una foto por cada página")
        onNodeWithText("Prefiero llenarla a mano").performClick()
        fillVaccine(date, types)
        waitForText("Agregar vacuna")
    }

    private fun ComposeUiTest.fillVaccine(date: LocalDate, types: List<String>) {
        waitForText("Nueva vacuna")
        if ("Rabia" in types) waitForText("Rabia")
        types.forEach { onNodeWithText(it).performScrollTo().performClick() }
        dates.addLast(date)
        onNode(hasContentDescription("Fecha", substring = true)).performScrollTo().performClick()
        screenshot("vacuna-${date}")
        onNodeWithText("Guardar vacuna").performClick()
    }

    /** Primeros [count] bytes de un archivo como texto (firma del formato). */
    private fun readStart(path: String, count: Int): String {
        val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return ""
        val bytes = ByteArray(data.length.toInt())
        bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return bytes.take(count).toByteArray().decodeToString()
    }

    /** JPEG de 1200x1600 de un color, como si fuera una foto de la galería. */
    private fun samplePhoto(index: Int): ByteArray {
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(1200.0, 1600.0))
        val image = renderer.imageWithActions { context ->
            UIColor(red = 0.2 + 0.3 * index, green = 0.5, blue = 0.4, alpha = 1.0).setFill()
            context?.fillRect(CGRectMake(0.0, 0.0, 1200.0, 1600.0))
        }
        val data = requireNotNull(UIImageJPEGRepresentation(image, 0.9))
        val bytes = ByteArray(data.length.toInt())
        bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return bytes
    }

    private fun ComposeUiTest.waitForText(text: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun SemanticsNodeInteraction.assertExistsSafely() {
        fetchSemanticsNode()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ComposeUiTest.screenshot(name: String) {
        val dir = NSProcessInfo.processInfo.environment["BESTID_SCREENSHOTS"] as? String ?: return
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        waitForIdle()
        val bitmap = onRoot().captureToImage().asSkiaBitmap()
        val png = Image.makeFromBitmap(bitmap).encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
        val file = fopen("$dir/$name.png", "wb") ?: return
        png.usePinned { fwrite(it.addressOf(0), 1u, png.size.toULong(), file) }
        fclose(file)
    }
}
