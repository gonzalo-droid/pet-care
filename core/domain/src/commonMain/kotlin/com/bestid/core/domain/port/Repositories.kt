package com.bestid.core.domain.port

import com.bestid.core.model.Pet
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
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
