package com.bestid.core.data.repository

import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.db.toEntity
import com.bestid.core.data.db.toModel
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.domain.port.VaccineTypeRepository
import com.bestid.core.domain.port.VetRepository
import com.bestid.core.model.Pet
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomPetRepository(db: BestIdDatabase) : PetRepository {
    private val dao = db.petDao()
    override fun observePets(): Flow<List<Pet>> = dao.observeActive().map { list -> list.map { it.toModel() } }
    override fun observePet(id: String): Flow<Pet?> = dao.observe(id).map { it?.toModel() }
    override suspend fun get(id: String): Pet? = dao.get(id)?.toModel()
    override suspend fun upsert(pet: Pet) = dao.upsert(pet.toEntity())
}

class RoomVetRepository(db: BestIdDatabase) : VetRepository {
    private val dao = db.vetDao()
    override fun observeAll(): Flow<List<Vet>> = dao.observeActive().map { list -> list.map { it.toModel() } }
    override suspend fun get(id: String): Vet? = dao.get(id)?.toModel()
    override suspend fun upsert(vet: Vet) = dao.upsert(vet.toEntity())
}

/** Asegura el catálogo semilla antes de la primera lectura (insertar ignora los que ya existen). */
class RoomVaccineTypeRepository(db: BestIdDatabase) : VaccineTypeRepository {
    private val dao = db.vaccineTypeDao()
    private var seeded = false

    private suspend fun ensureSeed() {
        if (!seeded) {
            dao.insertAll(VaccineCatalog.seed.map { it.toEntity() })
            seeded = true
        }
    }

    override fun observeAll(): Flow<List<VaccineType>> =
        dao.observeAll().onStart { ensureSeed() }.map { list -> list.map { it.toModel() } }

    override suspend fun getAll(): List<VaccineType> {
        ensureSeed()
        return dao.getAll().map { it.toModel() }
    }

    override suspend fun insert(type: VaccineType) = dao.insertAll(listOf(type.toEntity()))
}

class RoomVaccineRecordRepository(db: BestIdDatabase) : VaccineRecordRepository {
    private val dao = db.vaccineRecordDao()
    override fun observeForPet(petId: String): Flow<List<VaccineRecord>> =
        dao.observeForPet(petId).map { list -> list.map { it.toModel() } }
    override fun observe(id: String): Flow<VaccineRecord?> = dao.observe(id).map { it?.toModel() }
    override suspend fun get(id: String): VaccineRecord? = dao.get(id)?.toModel()
    override suspend fun upsert(record: VaccineRecord) = dao.upsert(record.toEntity())
}
