package com.bestid.core.domain

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.IdGenerator
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.SelectedPetStore
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.domain.port.VaccineTypeRepository
import com.bestid.core.domain.port.VetRepository
import com.bestid.core.model.Pet
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

class FakeClock(var today: LocalDate = LocalDate(2026, 11, 13)) : AppClock {
    override fun now(): Instant = today.atStartOfDayIn(TimeZone.UTC)
    override fun today(): LocalDate = today
}

class SequentialIds : IdGenerator {
    private var next = 1
    override fun newId(): String = "id-${next++}"
}

class FakePetRepository : PetRepository {
    val all = MutableStateFlow<List<Pet>>(emptyList())
    override fun observePets(): Flow<List<Pet>> = all.map { list -> list.filter { it.deletedAt == null } }
    override fun observePet(id: String): Flow<Pet?> = all.map { list -> list.firstOrNull { it.id == id && it.deletedAt == null } }
    override suspend fun get(id: String): Pet? = all.value.firstOrNull { it.id == id }
    override suspend fun getAllIncludingDeleted(): List<Pet> = all.value
    override suspend fun upsert(pet: Pet) {
        all.value = all.value.filterNot { it.id == pet.id } + pet
    }
}

class FakeRecordRepository : VaccineRecordRepository {
    val all = MutableStateFlow<List<VaccineRecord>>(emptyList())
    override fun observeAll(): Flow<List<VaccineRecord>> = all.map { list -> list.filter { it.deletedAt == null } }
    override fun observeForPet(petId: String): Flow<List<VaccineRecord>> =
        all.map { list -> list.filter { it.petId == petId && it.deletedAt == null }.sortedByDescending { it.applicationDate } }
    override fun observe(id: String): Flow<VaccineRecord?> = all.map { list -> list.firstOrNull { it.id == id && it.deletedAt == null } }
    override suspend fun get(id: String): VaccineRecord? = all.value.firstOrNull { it.id == id }
    override suspend fun getAllIncludingDeleted(): List<VaccineRecord> = all.value
    override suspend fun upsert(record: VaccineRecord) {
        all.value = all.value.filterNot { it.id == record.id } + record
    }
}

class FakeTypeRepository(seed: List<VaccineType> = emptyList()) : VaccineTypeRepository {
    val all = MutableStateFlow(seed)
    override fun observeAll(): Flow<List<VaccineType>> = all
    override suspend fun getAll(): List<VaccineType> = all.value
    override suspend fun insert(type: VaccineType) {
        all.value = all.value + type
    }
}

class FakeVetRepository : VetRepository {
    val all = MutableStateFlow<List<Vet>>(emptyList())
    override fun observeAll(): Flow<List<Vet>> = all
    override suspend fun get(id: String): Vet? = all.value.firstOrNull { it.id == id }
    override suspend fun upsert(vet: Vet) {
        all.value = all.value.filterNot { it.id == vet.id } + vet
    }
}

class FakeSelectedPetStore : SelectedPetStore {
    val value = MutableStateFlow<String?>(null)
    override val selectedPetId: Flow<String?> = value
    override suspend fun select(petId: String?) {
        value.value = petId
    }
}

class FakeCareRepository : com.bestid.core.domain.port.CareTaskRepository {
    val all = MutableStateFlow<List<com.bestid.core.model.CareTask>>(emptyList())
    override fun observeForPet(petId: String) = all.map { l -> l.filter { it.petId == petId && it.deletedAt == null } }
    override fun observeAll() = all.map { l -> l.filter { it.deletedAt == null } }
    override fun observe(id: String) = all.map { l -> l.firstOrNull { it.id == id && it.deletedAt == null } }
    override suspend fun get(id: String) = all.value.firstOrNull { it.id == id }
    override suspend fun upsert(task: com.bestid.core.model.CareTask) {
        all.value = all.value.filterNot { it.id == task.id } + task
    }
}

class FakeWeightRepository : com.bestid.core.domain.port.WeightRepository {
    val all = MutableStateFlow<List<com.bestid.core.model.WeightEntry>>(emptyList())
    override fun observeForPet(petId: String) = all.map { l -> l.filter { it.petId == petId && it.deletedAt == null }.sortedBy { it.date } }
    override suspend fun get(id: String) = all.value.firstOrNull { it.id == id }
    override suspend fun upsert(entry: com.bestid.core.model.WeightEntry) {
        all.value = all.value.filterNot { it.id == entry.id } + entry
    }
}

class FakeSettingsStore : com.bestid.core.domain.port.ReminderSettingsStore {
    val time = MutableStateFlow(kotlinx.datetime.LocalTime(9, 0))
    val shown = MutableStateFlow(false)
    override val reminderTime: Flow<kotlinx.datetime.LocalTime> = time
    override val permissionPromptShown: Flow<Boolean> = shown
    override suspend fun setReminderTime(time: kotlinx.datetime.LocalTime) { this.time.value = time }
    override suspend fun markPermissionPromptShown() { shown.value = true }
    val test = MutableStateFlow<kotlin.time.Instant?>(null)
    override val testReminderAt: Flow<kotlin.time.Instant?> = test
    override suspend fun setTestReminderAt(at: kotlin.time.Instant?) { test.value = at }
}

class FakeScheduledRepository : com.bestid.core.domain.port.ScheduledNotificationRepository {
    val all = MutableStateFlow<List<com.bestid.core.model.ScheduledNotification>>(emptyList())
    override suspend fun getAll() = all.value
    override suspend fun replace(added: List<com.bestid.core.model.ScheduledNotification>, removedIds: List<String>) {
        all.value = all.value.filterNot { it.id in removedIds } + added
    }
    override suspend fun clear() { all.value = emptyList() }
}

class FakeScheduler(var permission: com.bestid.core.domain.port.NotificationPermission = com.bestid.core.domain.port.NotificationPermission.NOT_DETERMINED) :
    com.bestid.core.domain.port.NotificationScheduler {
    val scheduled = mutableMapOf<String, com.bestid.core.domain.reminder.PlannedReminder>()
    val scheduleCalls = mutableListOf<List<String>>()
    val cancelCalls = mutableListOf<List<String>>()
    override suspend fun permission() = permission
    override suspend fun schedule(reminders: List<com.bestid.core.domain.reminder.PlannedReminder>) {
        scheduleCalls += reminders.map { it.id }
        reminders.forEach { scheduled[it.id] = it }
    }
    override suspend fun cancel(ids: List<String>) {
        cancelCalls += ids
        ids.forEach { scheduled.remove(it) }
    }
}

/** "Procesa" devolviendo los mismos bytes; los que empiezan con 0 no son imagen. */
class FakeImageProcessor : com.bestid.core.domain.port.ImageProcessor {
    override suspend fun process(original: ByteArray) =
        if (original.isEmpty() || original[0] == 0.toByte()) null
        else com.bestid.core.domain.port.ProcessedImage(original, width = 2000, height = 1500)
}

class FakePhotoFileStore : com.bestid.core.domain.port.PhotoFileStore {
    val files = mutableMapOf<String, ByteArray>()
    override suspend fun write(relativePath: String, bytes: ByteArray) {
        files[relativePath] = bytes
    }
    override suspend fun delete(relativePath: String) {
        files.remove(relativePath)
    }
    override suspend fun list(): List<String> = files.keys.toList()
    override fun absolutePath(relativePath: String) = "/app/$relativePath"
}

class FakePhotoRepository : com.bestid.core.domain.port.PhotoRepository {
    val all = MutableStateFlow<List<com.bestid.core.model.Photo>>(emptyList())
    override suspend fun insert(photo: com.bestid.core.model.Photo) {
        all.value = all.value + photo
    }
    override suspend fun get(ids: List<String>) = all.value.filter { it.id in ids }
    override suspend fun getAll() = all.value
    override suspend fun delete(ids: List<String>) {
        all.value = all.value.filterNot { it.id in ids }
    }
}
