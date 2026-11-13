package com.bestid.core.domain.di

import com.bestid.core.domain.usecase.DeletePet
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.usecase.GetVet
import com.bestid.core.domain.usecase.ObservePet
import com.bestid.core.domain.usecase.ObservePets
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecord
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.domain.usecase.ObserveVets
import com.bestid.core.domain.usecase.RestorePet
import com.bestid.core.domain.usecase.RestoreVaccineRecord
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SelectPet
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Casos de uso. Dependen solo de los puertos, que aporta `core/data`. */
val domainModule = module {
    factoryOf(::ObservePets)
    factoryOf(::ObservePet)
    factoryOf(::ObserveSelectedPet)
    factoryOf(::SelectPet)
    factoryOf(::SavePet)
    factoryOf(::DeletePet)
    factoryOf(::RestorePet)
    factoryOf(::ObserveVaccineRecords)
    factoryOf(::ObserveVaccineRecord)
    factoryOf(::ObserveVaccineTypes)
    factoryOf(::ObserveVets)
    factoryOf(::GetVet)
    factoryOf(::SaveVaccineRecord)
    factoryOf(::DeleteVaccineRecord)
    factoryOf(::RestoreVaccineRecord)
}
