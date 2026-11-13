package com.bestid.core.domain.reminder

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.CareTaskRepository
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.ReminderSettingsStore
import com.bestid.core.domain.port.ScheduledNotificationRepository
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.domain.port.VaccineTypeRepository
import com.bestid.core.model.ScheduledNotification
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Calcula los avisos deseados y aplica en el sistema solo la diferencia con lo programado
 * (sección 11). Con [force] reprograma todo (tras reiniciar el teléfono en Android).
 */
class SyncReminders(
    private val pets: PetRepository,
    private val records: VaccineRecordRepository,
    private val cares: CareTaskRepository,
    private val types: VaccineTypeRepository,
    private val settings: ReminderSettingsStore,
    private val scheduled: ScheduledNotificationRepository,
    private val scheduler: NotificationScheduler,
    private val clock: AppClock,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    private val mutex = Mutex()

    suspend operator fun invoke(force: Boolean = false) = mutex.withLock {
        val desired = ReminderEngine.plan(
            ReminderInput(
                pets = pets.observePets().first(),
                records = records.observeAll().first(),
                cares = cares.observeAll().first(),
                types = types.getAll().associateBy { it.id },
                reminderTime = settings.reminderTime.first(),
                now = clock.now(),
                timeZone = timeZone(),
            ),
        )
        val current = scheduled.getAll()
        if (force) {
            scheduler.cancel(current.map { it.id })
            scheduled.clear()
        }
        val diff = ReminderDiff.between(desired, if (force) emptySet() else current.map { it.id }.toSet())
        if (diff.toCancel.isNotEmpty()) scheduler.cancel(diff.toCancel)
        if (diff.toSchedule.isNotEmpty()) scheduler.schedule(diff.toSchedule)
        scheduled.replace(
            added = diff.toSchedule.map { ScheduledNotification(it.id, it.refType.name, it.refId, it.fireAt) },
            removedIds = diff.toCancel,
        )
    }
}

/**
 * Mantiene los avisos al día mientras la app está abierta: vuelve a sincronizar cuando cambian
 * mascotas, vacunas, cuidados o la hora preferida (y una vez al empezar, que cubre "al abrir la app").
 */
class ReminderCoordinator(
    private val pets: PetRepository,
    private val records: VaccineRecordRepository,
    private val cares: CareTaskRepository,
    private val settings: ReminderSettingsStore,
    private val sync: SyncReminders,
) {
    @OptIn(FlowPreview::class)
    suspend fun run() {
        combine(pets.observePets(), records.observeAll(), cares.observeAll(), settings.reminderTime) { _, _, _, _ -> }
            .debounce(DEBOUNCE)
            .collect { sync() }
    }

    private companion object {
        val DEBOUNCE = 500.milliseconds
    }
}

/**
 * La pantalla de permiso (O5) se muestra una sola vez, después del primer logro (guardar algo con
 * aviso) y solo si el sistema todavía no preguntó.
 */
class ShouldAskNotificationPermission(
    private val settings: ReminderSettingsStore,
    private val scheduler: NotificationScheduler,
) {
    suspend operator fun invoke(): Boolean =
        !settings.permissionPromptShown.first() && scheduler.permission() == NotificationPermission.NOT_DETERMINED
}

class MarkNotificationPromptShown(private val settings: ReminderSettingsStore) {
    suspend operator fun invoke() = settings.markPermissionPromptShown()
}

class ObserveNotificationPermission(private val scheduler: NotificationScheduler) {
    suspend operator fun invoke(): NotificationPermission = scheduler.permission()
}

/**
 * Herramienta de desarrollo: programa un aviso de prueba en 1 minuto para comprobar que llega con
 * la app cerrada y tras reiniciar. No pasa por la tabla de programados.
 */
class ScheduleTestReminder(
    private val scheduler: NotificationScheduler,
    private val pets: PetRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(subjectLabel: String?) {
        val pet = pets.observePets().first().firstOrNull()
        val now = clock.now()
        scheduler.schedule(
            listOf(
                PlannedReminder(
                    id = "debug:test:${now.toEpochMilliseconds()}",
                    refType = ReminderRefType.VACCINE,
                    refId = "",
                    petId = pet?.id.orEmpty(),
                    petName = pet?.name ?: "BestID",
                    subject = ReminderSubject.Vaccine(subjectLabel),
                    kind = ReminderKind.SAME_DAY,
                    dueDate = clock.today(),
                    fireAt = now + 60.seconds,
                ),
            ),
        )
    }
}

/** Hora por defecto de los avisos (sección 11). */
val DEFAULT_REMINDER_TIME = LocalTime(9, 0)
