package com.bestid.feature.settings

import app.cash.turbine.test
import com.bestid.core.domain.FakeBackupArchive
import com.bestid.core.domain.FakeBackupSettings
import com.bestid.core.domain.FakeBackupStore
import com.bestid.core.domain.FakeCareRepository
import com.bestid.core.domain.FakeClock
import com.bestid.core.domain.FakePetRepository
import com.bestid.core.domain.FakePhotoFileStore
import com.bestid.core.domain.FakePhotoRepository
import com.bestid.core.domain.FakeRecordRepository
import com.bestid.core.domain.FakeScheduledRepository
import com.bestid.core.domain.FakeScheduler
import com.bestid.core.domain.FakeSettingsStore
import com.bestid.core.domain.FakeTempFiles
import com.bestid.core.domain.FakeTypeRepository
import com.bestid.core.domain.FakeVetRepository
import com.bestid.core.domain.FakeWeightRepository
import com.bestid.core.domain.backup.ExportBackup
import com.bestid.core.domain.backup.ImportBackup
import com.bestid.core.domain.usecase.ObserveLastBackup
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.backup.BackupData
import com.bestid.core.domain.port.AppInfo
import com.bestid.core.domain.port.BackupStore
import com.bestid.core.domain.reminder.SyncReminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** TST-05: Respaldo (A2). Un error al exportar se avisa sin dejar archivo; un archivo que no es respaldo no se importa. */
@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    private val clock = FakeClock()
    private val pets = FakePetRepository()
    private val vets = FakeVetRepository()
    private val types = FakeTypeRepository(VaccineCatalog.seed)
    private val records = FakeRecordRepository()
    private val cares = FakeCareRepository()
    private val photos = FakePhotoRepository()
    private val files = FakePhotoFileStore()
    private val archive = FakeBackupArchive(files)
    private val settings = FakeBackupSettings()
    private val store = FakeBackupStore(pets, vets, types, records, cares, FakeWeightRepository(), photos)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /** Sin espacio, por ejemplo: leer la base falla. */
    private val brokenStore = object : BackupStore by store {
        override suspend fun snapshot(): BackupData = throw IllegalStateException("sin espacio")
    }

    private fun viewModel(exportFrom: BackupStore = store) = BackupViewModel(
        observeLast = ObserveLastBackup(settings),
        export = ExportBackup(exportFrom, archive, FakeTempFiles(), settings, clock, object : AppInfo { override val versionName = "0.6.0" }),
        import = ImportBackup(
            archive,
            store,
            photos,
            files,
            SyncReminders(pets, records, cares, types, FakeSettingsStore(), FakeScheduledRepository(), FakeScheduler(), clock) { TimeZone.UTC },
        ),
        clock = clock,
    )

    // El caso feliz de exportar lo cubre BackupTest (dominio): aquí el nombre del archivo sale de Compose
    // Resources, que no carga en el host de Android.
    @Test
    fun aFailedExportShowsTheMessageAndNoFile() = runTest {
        val vm = viewModel(exportFrom = brokenStore)
        vm.state.test {
            vm.exportBackup()
            val state = expectMostRecentItem()
            assertFalse(state.exporting)
            assertNull(state.readyPath)
            assertEquals(BackupMessage.ExportFailed, state.message)
        }
    }

    @Test
    fun aFileThatIsNotABackupIsRejected() = runTest {
        val vm = viewModel()
        vm.state.test {
            vm.importBackup("/tmp/no-es-un-respaldo.zip")
            val state = expectMostRecentItem()
            assertFalse(state.importing)
            assertEquals(BackupMessage.Invalid, state.message)
        }
    }
}
