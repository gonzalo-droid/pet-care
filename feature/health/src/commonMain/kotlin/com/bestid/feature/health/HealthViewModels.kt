package com.bestid.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.reminder.ObserveNotificationPermission
import com.bestid.core.domain.reminder.ShouldAskNotificationPermission
import com.bestid.core.domain.usecase.CareDraft
import com.bestid.core.domain.usecase.DeleteCareTask
import com.bestid.core.domain.usecase.DeleteWeight
import com.bestid.core.domain.usecase.ObserveCareTask
import com.bestid.core.domain.usecase.ObserveCareTasks
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveWeights
import com.bestid.core.domain.usecase.SaveCareResult
import com.bestid.core.domain.usecase.SaveCareTask
import com.bestid.core.domain.usecase.SaveWeight
import com.bestid.core.domain.usecase.SaveWeightResult
import com.bestid.core.domain.usecase.SetCareReminder
import com.bestid.core.domain.validation.CareError
import com.bestid.core.domain.validation.HealthValidation
import com.bestid.core.domain.validation.WeightError
import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.Pet
import com.bestid.core.model.WeightEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import com.bestid.core.ui.Retry
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.bestid.core.ui.attempt
import kotlinx.datetime.LocalDate

data class HealthState(
    val loading: Boolean = true,
    /** No se pudieron leer los datos: se muestra el error con "Reintentar". */
    val failed: Boolean = false,
    val pet: Pet? = null,
    val cares: List<CareTask> = emptyList(),
    val weights: List<WeightEntry> = emptyList(),
    val today: LocalDate? = null,
    val permission: NotificationPermission? = null,
    /** Se borró un peso: la pantalla ofrece "Deshacer". */
    val deletedWeightId: String? = null,
)

/** Salud (S1): cuidados con su aviso y peso con gráfico de la mascota seleccionada. */
@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModel(
    observeSelectedPet: ObserveSelectedPet,
    observeCares: ObserveCareTasks,
    observeWeights: ObserveWeights,
    private val setCareReminder: SetCareReminder,
    private val deleteWeight: DeleteWeight,
    private val observePermission: ObserveNotificationPermission,
    private val clock: AppClock,
) : ViewModel() {
    private val permission = MutableStateFlow<NotificationPermission?>(null)
    private val deletedWeight = MutableStateFlow<String?>(null)

    private val retryHealthState = Retry()

    val state: StateFlow<HealthState> = retryHealthState.wrap(HealthState(loading = false, failed = true)) {
        combine(
            observeSelectedPet().flatMapLatest { pet ->
                if (pet == null) {
                    flowOf(Triple<Pet?, List<CareTask>, List<WeightEntry>>(null, emptyList(), emptyList()))
                } else {
                    combine(observeCares(pet.id), observeWeights(pet.id)) { cares, weights -> Triple(pet, cares, weights) }
                }
            },
            permission,
            deletedWeight,
        ) { (pet, cares, weights), perm, deleted ->
            HealthState(loading = false, pet = pet, cares = cares, weights = weights, today = clock.today(), permission = perm, deletedWeightId = deleted)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthState())

    fun retry() = retryHealthState.retry()

    /** Se vuelve a leer al volver a la pantalla (por si se cambió en los ajustes del sistema). */
    fun refreshPermission() {
        viewModelScope.launch { permission.value = observePermission() }
    }

    fun setReminder(careId: String, enabled: Boolean) {
        viewModelScope.launch { setCareReminder(careId, enabled) }
    }

    fun deleteWeight(id: String) {
        viewModelScope.launch {
            deleteWeight.invoke(id)
            deletedWeight.value = id
        }
    }

    fun undoShown() {
        deletedWeight.value = null
    }
}

data class CareFormState(
    /** El guardado falló (por ejemplo, sin espacio): se muestra el aviso y el formulario sigue abierto. */
    val saveFailed: Boolean = false,
    val loaded: Boolean = false,
    val isEdit: Boolean = false,
    val petId: String? = null,
    val kind: CareKind = CareKind.DEWORM_INTERNAL,
    val title: String = "",
    val lastDate: LocalDate? = null,
    val intervalText: String = "",
    val unit: IntervalUnit = IntervalUnit.MONTH,
    val reminderEnabled: Boolean = true,
    val errors: List<CareError> = emptyList(),
    val saving: Boolean = false,
    val done: Boolean = false,
    /** Primer logro: mostrar la pantalla de permiso de avisos (O5) al cerrar. */
    val askPermission: Boolean = false,
    val deletedId: String? = null,
)

/** Agregar o editar cuidado (S2). */
class CareFormViewModel(
    private val careId: String?,
    private val observeSelectedPet: ObserveSelectedPet,
    private val observeCare: ObserveCareTask,
    private val saveCare: SaveCareTask,
    private val deleteCare: DeleteCareTask,
    private val shouldAskPermission: ShouldAskNotificationPermission,
) : ViewModel() {
    private val _state = MutableStateFlow(CareFormState())
    val state: StateFlow<CareFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val care = careId?.let { observeCare(it).first() }
            _state.value = if (care == null) {
                CareFormState(loaded = true, petId = observeSelectedPet().first()?.id)
            } else {
                CareFormState(
                    loaded = true,
                    isEdit = true,
                    petId = care.petId,
                    kind = care.kind,
                    title = care.title.orEmpty(),
                    lastDate = care.lastDate,
                    intervalText = care.intervalValue?.toString().orEmpty(),
                    unit = care.intervalUnit ?: IntervalUnit.MONTH,
                    reminderEnabled = care.reminderEnabled,
                )
            }
        }
    }

    fun onKind(kind: CareKind) = _state.update { it.copy(kind = kind, errors = emptyList()) }
    fun onTitle(value: String) = _state.update { it.copy(title = value, errors = it.errors - CareError.TITLE_REQUIRED) }
    fun onLastDate(value: LocalDate) = _state.update { it.copy(lastDate = value, errors = emptyList()) }
    fun onInterval(value: String) = _state.update { it.copy(intervalText = value.filter(Char::isDigit).take(3), errors = it.errors - CareError.INTERVAL_REQUIRED) }
    fun onUnit(value: IntervalUnit) = _state.update { it.copy(unit = value) }
    fun onReminder(value: Boolean) = _state.update { it.copy(reminderEnabled = value, errors = emptyList()) }

    fun save() {
        val s = _state.value
        val petId = s.petId ?: return
        if (s.saving) return
        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val result = attempt { saveCare(
                CareDraft(
                    id = careId,
                    petId = petId,
                    kind = s.kind,
                    title = s.title,
                    lastDate = s.lastDate,
                    intervalValue = s.intervalText.toIntOrNull(),
                    intervalUnit = s.unit,
                    reminderEnabled = s.reminderEnabled,
                ),
            ) }.getOrElse {
                _state.update { it.copy(saving = false, saveFailed = true) }
                return@launch
            }
            when (result) {
                is SaveCareResult.Saved -> {
                    val ask = result.task.reminderEnabled && result.task.nextDate != null && shouldAskPermission()
                    _state.update { it.copy(saving = false, done = true, askPermission = ask) }
                }
                is SaveCareResult.Invalid -> _state.update { it.copy(saving = false, errors = result.errors) }
            }
        }
    }

    fun delete() {
        val id = careId ?: return
        viewModelScope.launch {
            deleteCare(id)
            _state.update { it.copy(done = true, deletedId = id) }
        }
    }
}

data class WeightFormState(
    /** El guardado falló (por ejemplo, sin espacio): se muestra el aviso y el formulario sigue abierto. */
    val saveFailed: Boolean = false,
    val petId: String? = null,
    val date: LocalDate? = null,
    val kgText: String = "",
    val error: WeightError? = null,
    val saving: Boolean = false,
    val done: Boolean = false,
)

/** Agregar peso (S3): fecha (hoy por defecto) y peso en kg con coma decimal. */
class WeightFormViewModel(
    private val observeSelectedPet: ObserveSelectedPet,
    private val saveWeight: SaveWeight,
    clock: AppClock,
) : ViewModel() {
    private val _state = MutableStateFlow(WeightFormState(date = clock.today()))
    val state: StateFlow<WeightFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.update { it.copy(petId = observeSelectedPet().first()?.id) } }
    }

    fun onDate(value: LocalDate) = _state.update { it.copy(date = value, error = null) }
    fun onKg(value: String) = _state.update { it.copy(kgText = value.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(7), error = null) }

    fun save() {
        val s = _state.value
        val petId = s.petId ?: return
        val date = s.date ?: return
        if (s.saving) return
        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val result = attempt { saveWeight(petId, date, HealthValidation.parseKilograms(s.kgText)) }.getOrElse {
                _state.update { it.copy(saving = false, saveFailed = true) }
                return@launch
            }
            when (result) {
                is SaveWeightResult.Saved -> _state.update { it.copy(saving = false, done = true) }
                is SaveWeightResult.Invalid -> _state.update { it.copy(saving = false, error = result.error) }
            }
        }
    }
}
