package com.bestid.core.platform.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSLocale
import platform.Foundation.NSSelectorFromString
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIColor
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.navigationItem
import platform.UIKit.systemBackgroundColor

@Composable
internal actual fun rememberPlatformDatePickerLauncher(onPicked: (LocalDate) -> Unit): DatePickerLauncher {
    val callback by rememberUpdatedState(onPicked)
    return remember {
        object : DatePickerLauncher {
            override fun launch(initial: LocalDate?, max: LocalDate?) {
                val presenter = topViewController() ?: return
                val picker = DatePickerViewController(initial, max) { callback(it) }
                val navigation = UINavigationController(rootViewController = picker)
                // Sin detent medio: el calendario inline en media hoja entra en un ciclo de layout que
                // congela el hilo principal y iOS cierra la app (TestFlight 0.5.0 (1)). La hoja va completa.
                presenter.presentViewController(navigation, animated = true, completion = null)
            }
        }
    }
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    while (true) top = top.presentedViewController ?: return top
}

/** Hoja con un `UIDatePicker` en calendario, "Cancelar" y "Listo", en español de Perú. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class DatePickerViewController(
    private val initial: LocalDate?,
    private val max: LocalDate?,
    private val onDone: (LocalDate) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val picker = UIDatePicker()

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.systemBackgroundColor
        picker.datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        picker.preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleInline
        picker.locale = NSLocale(localeIdentifier = "es_PE")
        initial?.let { picker.date = it.toNSDate() }
        max?.let { picker.maximumDate = it.toNSDate() }
        picker.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(picker)
        NSLayoutConstraint.activateConstraints(
            listOf(
                picker.topAnchor.constraintEqualToAnchor(view.safeAreaLayoutGuide.topAnchor),
                picker.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor, constant = 16.0),
                picker.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor, constant = -16.0),
            ),
        )
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemCancel,
            target = this,
            action = NSSelectorFromString("cancel"),
        )
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemDone,
            target = this,
            action = NSSelectorFromString("done"),
        )
    }

    @ObjCAction
    fun cancel() {
        dismissViewControllerAnimated(true, completion = null)
    }

    @ObjCAction
    fun done() {
        onDone(picker.date.toLocalDate())
        dismissViewControllerAnimated(true, completion = null)
    }
}

private fun LocalDate.toNSDate(): NSDate {
    val components = NSDateComponents().apply {
        setYear(this@toNSDate.year.toLong())
        setMonth(this@toNSDate.month.number.toLong())
        setDay(this@toNSDate.day.toLong())
        setHour(12)
    }
    return requireNotNull(NSCalendar.currentCalendar.dateFromComponents(components))
}

private fun NSDate.toLocalDate(): LocalDate {
    val c = NSCalendar.currentCalendar.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay, fromDate = this)
    return LocalDate(c.year.toInt(), c.month.toInt(), c.day.toInt())
}
