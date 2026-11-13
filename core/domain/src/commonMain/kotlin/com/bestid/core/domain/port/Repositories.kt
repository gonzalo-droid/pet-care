package com.bestid.core.domain.port

import com.bestid.core.model.CareTask
import com.bestid.core.model.Pet
import com.bestid.core.model.ScheduledNotification
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import com.bestid.core.model.WeightEntry
import kotlinx.datetime.LocalTime
import kotlinx.coroutines.flow.Flow

/**
 * Puertos de persistencia. Las implementaciones viven en `core/data` (Room y DataStore).
 * Las lecturas `observe*` excluyen lo borrado lógicamente (`deletedAt != null`).
 */
interface PetRepository {
    /** Mascotas activas, en orden de creación. */
    fun observePets(): Flow<List<Pet>>
    fun observePet(id: String): Flow<Pet?>
    /** Incluye mascotas borradas, para poder restaurarlas. */
    suspend fun get(id: String): Pet?
    suspend fun upsert(pet: Pet)
}

interface VaccineRecordRepository {
    /** Todos los registros activos (para el motor de avisos). */
    fun observeAll(): Flow<List<VaccineRecord>>
    /** Registros activos de una mascota, del más reciente al más antiguo. */
    fun observeForPet(petId: String): Flow<List<VaccineRecord>>
    fun observe(id: String): Flow<VaccineRecord?>
    /** Incluye registros borrados, para poder restaurarlos. */
    suspend fun get(id: String): VaccineRecord?
    suspend fun upsert(record: VaccineRecord)
}

interface VaccineTypeRepository {
    /** Catálogo semilla y tipos escritos a mano. */
    fun observeAll(): Flow<List<VaccineType>>
    suspend fun getAll(): List<VaccineType>
    suspend fun insert(type: VaccineType)
}

interface VetRepository {
    fun observeAll(): Flow<List<Vet>>
    suspend fun get(id: String): Vet?
    suspend fun upsert(vet: Vet)
}

/** Mascota seleccionada, recordada entre sesiones (DataStore). */
interface SelectedPetStore {
    val selectedPetId: Flow<String?>
    suspend fun select(petId: String?)
}

interface CareTaskRepository {
    /** Cuidados activos de una mascota, por próxima fecha. */
    fun observeForPet(petId: String): Flow<List<CareTask>>
    /** Todos los cuidados activos (para el motor de avisos). */
    fun observeAll(): Flow<List<CareTask>>
    fun observe(id: String): Flow<CareTask?>
    suspend fun get(id: String): CareTask?
    suspend fun upsert(task: CareTask)
}

interface WeightRepository {
    /** Pesos activos de una mascota, del más antiguo al más reciente. */
    fun observeForPet(petId: String): Flow<List<WeightEntry>>
    suspend fun get(id: String): WeightEntry?
    suspend fun upsert(entry: WeightEntry)
}

/** Lo que está programado en el sistema operativo, para sincronizar solo la diferencia. */
interface ScheduledNotificationRepository {
    suspend fun getAll(): List<ScheduledNotification>
    suspend fun replace(added: List<ScheduledNotification>, removedIds: List<String>)
    suspend fun clear()
}

/** Preferencias de avisos (DataStore). */
interface ReminderSettingsStore {
    /** Hora preferida de los avisos (9:00 por defecto). */
    val reminderTime: Flow<LocalTime>
    /** Si ya se mostró la pantalla de permiso de avisos (O5). */
    val permissionPromptShown: Flow<Boolean>
    suspend fun setReminderTime(time: LocalTime)
    suspend fun markPermissionPromptShown()

    /** Aviso de prueba (herramienta de desarrollo, D26): se guarda para sobrevivir a un reinicio. */
    val testReminderAt: Flow<kotlin.time.Instant?>
    suspend fun setTestReminderAt(at: kotlin.time.Instant?)
}
