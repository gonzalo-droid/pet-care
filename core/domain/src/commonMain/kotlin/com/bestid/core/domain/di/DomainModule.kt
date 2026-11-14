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
import com.bestid.core.domain.reminder.MarkNotificationPromptShown
import com.bestid.core.domain.reminder.ObserveNotificationPermission
import com.bestid.core.domain.reminder.ReminderCoordinator
import com.bestid.core.domain.reminder.ScheduleTestReminder
import com.bestid.core.domain.reminder.ShouldAskNotificationPermission
import com.bestid.core.domain.reminder.SyncReminders
import com.bestid.core.domain.usecase.DeleteCareTask
import com.bestid.core.domain.usecase.DeleteWeight
import com.bestid.core.domain.usecase.ObserveCareTask
import com.bestid.core.domain.usecase.ObserveCareTasks
import com.bestid.core.domain.usecase.ObserveWeights
import com.bestid.core.domain.usecase.RestoreCareTask
import com.bestid.core.domain.usecase.RestoreWeight
import com.bestid.core.domain.usecase.SaveCareTask
import com.bestid.core.domain.usecase.SaveWeight
import com.bestid.core.domain.usecase.SetCareReminder
import com.bestid.core.domain.usecase.CleanUpPhotos
import com.bestid.core.domain.usecase.GetPhotos
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.domain.usecase.ShareFilePaths
import com.bestid.core.domain.backup.DeleteAllData
import com.bestid.core.domain.backup.ExportBackup
import com.bestid.core.domain.backup.ImportBackup
import com.bestid.core.domain.share.BuildPetSheet
import com.bestid.core.domain.usecase.GetAppVersion
import com.bestid.core.domain.usecase.ObserveLastBackup
import com.bestid.core.domain.usecase.ObserveReminderTime
import com.bestid.core.domain.usecase.SetReminderTime
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
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

    factoryOf(::ObserveCareTasks)
    factoryOf(::ObserveCareTask)
    factoryOf(::SaveCareTask)
    factoryOf(::SetCareReminder)
    factoryOf(::DeleteCareTask)
    factoryOf(::RestoreCareTask)
    factoryOf(::ObserveWeights)
    factoryOf(::SaveWeight)
    factoryOf(::DeleteWeight)
    factoryOf(::RestoreWeight)

    factoryOf(::ImportPhotos)
    factoryOf(::GetPhotos)
    factory { CleanUpPhotos(get(), get(), get(), get(), get()) }

    factoryOf(::ExportBackup)
    factoryOf(::ImportBackup)
    factoryOf(::DeleteAllData)
    factoryOf(::BuildPetSheet)
    factoryOf(::ShareFilePaths)
    factoryOf(::ObserveReminderTime)
    factoryOf(::SetReminderTime)
    factoryOf(::ObserveLastBackup)
    factoryOf(::GetAppVersion)

    // Avisos: una sola instancia para que su Mutex evite sincronizaciones simultáneas.
    single { SyncReminders(get(), get(), get(), get(), get(), get(), get(), get()) }
    singleOf(::ReminderCoordinator)
    factoryOf(::ShouldAskNotificationPermission)
    factoryOf(::MarkNotificationPromptShown)
    factoryOf(::ObserveNotificationPermission)
    factoryOf(::ScheduleTestReminder)
}
