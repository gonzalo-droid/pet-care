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
    override suspend fun upsert(pet: Pet) {
        all.value = all.value.filterNot { it.id == pet.id } + pet
    }
}

class FakeRecordRepository : VaccineRecordRepository {
    val all = MutableStateFlow<List<VaccineRecord>>(emptyList())
    override fun observeForPet(petId: String): Flow<List<VaccineRecord>> =
        all.map { list -> list.filter { it.petId == petId && it.deletedAt == null }.sortedByDescending { it.applicationDate } }
    override fun observe(id: String): Flow<VaccineRecord?> = all.map { list -> list.firstOrNull { it.id == id && it.deletedAt == null } }
    override suspend fun get(id: String): VaccineRecord? = all.value.firstOrNull { it.id == id }
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
