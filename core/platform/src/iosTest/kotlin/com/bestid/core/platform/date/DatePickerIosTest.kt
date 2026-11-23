package com.bestid.core.platform.date

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.LocalDate
import platform.Foundation.performSelectorOnMainThread
import platform.UIKit.navigationItem
import platform.darwin.NSObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** El selector nativo no se puede tocar desde las pruebas de Compose: aquí se prueban sus botones como los toca UIKit. */
@OptIn(ExperimentalForeignApi::class)
class DatePickerIosTest {
    @Test
    fun listoDevuelveLaFechaElegida() {
        var picked: LocalDate? = null
        val controller = DatePickerViewController(LocalDate(2024, 3, 5), LocalDate(2026, 1, 1)) { picked = it }
        controller.loadViewIfNeeded()

        val done = assertNotNull(controller.navigationItem.rightBarButtonItem)
        val action = assertNotNull(done.action)
        assertTrue(controller.respondsToSelector(action), "UIKit no encuentra la acción de Listo")
        controller.tap(action)

        assertEquals(LocalDate(2024, 3, 5), picked)
    }

    @Test
    fun cancelarNoDevuelveFecha() {
        var picked: LocalDate? = null
        val controller = DatePickerViewController(null, null) { picked = it }
        controller.loadViewIfNeeded()

        val cancel = assertNotNull(controller.navigationItem.leftBarButtonItem)
        val action = assertNotNull(cancel.action)
        assertTrue(controller.respondsToSelector(action), "UIKit no encuentra la acción de Cancelar")
        controller.tap(action)

        assertEquals(null, picked)
    }
}

/** Como UIKit: la acción no devuelve nada. `performSelector` espera un objeto de vuelta y lee basura. */
@OptIn(ExperimentalForeignApi::class)
private fun NSObject.tap(action: COpaquePointer) = performSelectorOnMainThread(action, withObject = null, waitUntilDone = true)
