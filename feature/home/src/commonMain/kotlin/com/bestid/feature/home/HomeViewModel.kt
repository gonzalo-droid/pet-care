package com.bestid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.reminder.ObserveNotificationPermission
import com.bestid.core.domain.schedule.CareSchedule
import com.bestid.core.domain.schedule.NextDueCalculator
import com.bestid.core.domain.usecase.ObserveCareTasks
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.domain.usecase.ObserveWeights
import com.bestid.core.model.CareTask
import com.bestid.core.model.Pet
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.WeightEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import com.bestid.core.ui.Retry
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** Lo próximo en Inicio: una vacuna o un cuidado, el que venga antes. */
sealed interface Upcoming {
    val date: LocalDate
    val daysLeft: Int

    data class Vaccine(val record: VaccineRecord, override val date: LocalDate, override val daysLeft: Int) : Upcoming
    data class Care(val task: CareTask, override val date: LocalDate, override val daysLeft: Int) : Upcoming
}

data class HomeState(
    val loading: Boolean = true,
    /** No se pudieron leer los datos: se muestra el error con "Reintentar". */
    val failed: Boolean = false,
    val pet: Pet? = null,
    val vaccineCount: Int = 0,
    val upcoming: Upcoming? = null,
    val lastRecord: VaccineRecord? = null,
    val lastWeight: WeightEntry? = null,
    val types: Map<String, VaccineType> = emptyMap(),
    /** Hay algo con aviso pero el sistema lo tiene bloqueado. */
    val remindersBlocked: Boolean = false,
)

/** Inicio (H1): mascota, lo próximo con días restantes, última vacuna y último peso. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    observeSelectedPet: ObserveSelectedPet,
    observeRecords: ObserveVaccineRecords,
    observeTypes: ObserveVaccineTypes,
    observeCares: ObserveCareTasks,
    observeWeights: ObserveWeights,
    private val observePermission: ObserveNotificationPermission,
    clock: AppClock,
) : ViewModel() {
    private val permission = MutableStateFlow<NotificationPermission?>(null)

    private val retryHomeState = Retry()

    val state: StateFlow<HomeState> = retryHomeState.wrap(HomeState(loading = false, failed = true)) {
        observeSelectedPet()
            .flatMapLatest { pet ->
                if (pet == null) {
                    flowOf(HomeState(loading = false))
                } else {
                    combine(observeRecords(pet.id), observeTypes(), observeCares(pet.id), observeWeights(pet.id), permission) { records, types, cares, weights, perm ->
                        val today = clock.today()
                        val vaccine = NextDueCalculator.nextDue(records, today)?.let { Upcoming.Vaccine(it.record, it.date, it.daysLeft) }
                        val care = CareSchedule.nextDue(cares, today)?.let { Upcoming.Care(it.task, it.date, it.daysLeft) }
                        val hasReminders = vaccine?.record?.nextDateConfirmed == true || care != null
                        HomeState(
                            loading = false,
                            pet = pet,
                            vaccineCount = records.size,
                            upcoming = listOfNotNull(vaccine, care).sortedWith(upcomingOrder).firstOrNull(),
                            lastRecord = records.firstOrNull(),
                            lastWeight = weights.lastOrNull(),
                            types = types.associateBy { it.id },
                            remindersBlocked = hasReminders && perm == NotificationPermission.DENIED,
                        )
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun retry() = retryHomeState.retry()

    fun refreshPermission() {
        viewModelScope.launch { permission.value = observePermission() }
    }

    private companion object {
        /** Primero lo que viene (de hoy en adelante), luego lo atrasado. */
        val upcomingOrder = compareBy<Upcoming>({ it.daysLeft < 0 }, { kotlin.math.abs(it.daysLeft) })
    }
}
