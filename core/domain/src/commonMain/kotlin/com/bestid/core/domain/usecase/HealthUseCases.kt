package com.bestid.core.domain.usecase

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.CareTaskRepository
import com.bestid.core.domain.port.IdGenerator
import com.bestid.core.domain.port.WeightRepository
import com.bestid.core.domain.schedule.CareSchedule
import com.bestid.core.domain.validation.CareError
import com.bestid.core.domain.validation.HealthValidation
import com.bestid.core.domain.validation.WeightError
import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Datos del formulario de cuidado (S2). `id == null` crea uno nuevo. */
data class CareDraft(
    val id: String? = null,
    val petId: String,
    val kind: CareKind,
    val title: String? = null,
    val lastDate: LocalDate?,
    val intervalValue: Int?,
    val intervalUnit: IntervalUnit,
    val reminderEnabled: Boolean,
)

sealed interface SaveCareResult {
    data class Saved(val task: CareTask) : SaveCareResult
    data class Invalid(val errors: List<CareError>) : SaveCareResult
}

class ObserveCareTasks(private val repo: CareTaskRepository) {
    operator fun invoke(petId: String): Flow<List<CareTask>> = repo.observeForPet(petId)
}

class ObserveCareTask(private val repo: CareTaskRepository) {
    operator fun invoke(id: String): Flow<CareTask?> = repo.observe(id)
}

/** Crea o edita un cuidado y calcula su próxima fecha. La app no sugiere intervalos. */
class SaveCareTask(
    private val repo: CareTaskRepository,
    private val clock: AppClock,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(draft: CareDraft): SaveCareResult {
        val errors = HealthValidation.validateCare(draft.kind, draft.title, draft.lastDate, draft.intervalValue, draft.reminderEnabled, clock.today())
        if (errors.isNotEmpty()) return SaveCareResult.Invalid(errors)
        val now = clock.now()
        val existing = draft.id?.let { repo.get(it) }
        val task = CareTask(
            id = existing?.id ?: ids.newId(),
            petId = draft.petId,
            kind = draft.kind,
            title = if (draft.kind == CareKind.OTHER) draft.title.cleaned() else null,
            lastDate = draft.lastDate,
            intervalValue = draft.intervalValue,
            intervalUnit = draft.intervalUnit,
            nextDate = CareSchedule.nextDate(draft.lastDate, draft.intervalValue, draft.intervalUnit),
            reminderEnabled = draft.reminderEnabled,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            deletedAt = null,
        )
        repo.upsert(task)
        return SaveCareResult.Saved(task)
    }
}

/** Activa o desactiva el aviso de un cuidado desde la lista (S1). */
class SetCareReminder(private val repo: CareTaskRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String, enabled: Boolean) {
        val task = repo.get(id) ?: return
        repo.upsert(task.copy(reminderEnabled = enabled, updatedAt = clock.now()))
    }
}

class DeleteCareTask(private val repo: CareTaskRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String) {
        val task = repo.get(id) ?: return
        val now = clock.now()
        repo.upsert(task.copy(deletedAt = now, updatedAt = now))
    }
}

class RestoreCareTask(private val repo: CareTaskRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String) {
        val task = repo.get(id) ?: return
        repo.upsert(task.copy(deletedAt = null, updatedAt = clock.now()))
    }
}

sealed interface SaveWeightResult {
    data class Saved(val entry: WeightEntry) : SaveWeightResult
    data class Invalid(val error: WeightError) : SaveWeightResult
}

class ObserveWeights(private val repo: WeightRepository) {
    operator fun invoke(petId: String): Flow<List<WeightEntry>> = repo.observeForPet(petId)
}

/** Registra un peso (S3). El peso llega en gramos para evitar decimales (sección 9). */
class SaveWeight(
    private val repo: WeightRepository,
    private val clock: AppClock,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(petId: String, date: LocalDate, grams: Int?): SaveWeightResult {
        HealthValidation.validateWeight(grams, date, clock.today())?.let { return SaveWeightResult.Invalid(it) }
        val now = clock.now()
        val entry = WeightEntry(ids.newId(), petId, date, requireNotNull(grams), now, now, null)
        repo.upsert(entry)
        return SaveWeightResult.Saved(entry)
    }
}

class DeleteWeight(private val repo: WeightRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String) {
        val entry = repo.get(id) ?: return
        val now = clock.now()
        repo.upsert(entry.copy(deletedAt = now, updatedAt = now))
    }
}

class RestoreWeight(private val repo: WeightRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String) {
        val entry = repo.get(id) ?: return
        repo.upsert(entry.copy(deletedAt = null, updatedAt = clock.now()))
    }
}
