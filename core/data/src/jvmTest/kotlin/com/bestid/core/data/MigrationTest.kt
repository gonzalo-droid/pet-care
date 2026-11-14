package com.bestid.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.bestid.core.data.db.BestIdDatabase
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regla 8: cada cambio de esquema trae su migración, probada con los esquemas exportados. */
class MigrationTest {
    private val helper = MigrationTestHelper(
        schemaDirectoryPath = Path("schemas"),
        databasePath = Files.createTempDirectory("bestid-migration").resolve("test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = BestIdDatabase::class,
    )

    @Test
    fun migrate1To2KeepsDataAndAddsHealthTables() {
        helper.createDatabase(1).apply {
            execSQL(
                "INSERT INTO pets (id, name, species, createdAt, updatedAt) VALUES ('lilith', 'Lilith', 'CAT', 1, 1)",
            )
            close()
        }
        val db = helper.runMigrationsAndValidate(2)
        db.prepare("SELECT name FROM pets WHERE id = 'lilith'").use { st ->
            st.step()
            assertEquals("Lilith", st.getText(0))
        }
        db.execSQL("INSERT INTO care_tasks (id, petId, kind, reminderEnabled, createdAt, updatedAt) VALUES ('c', 'lilith', 'DEWORM_INTERNAL', 1, 1, 1)")
        db.execSQL("INSERT INTO weight_entries (id, petId, date, grams, createdAt, updatedAt) VALUES ('w', 'lilith', '2026-11-05', 3800, 1, 1)")
        db.execSQL("INSERT INTO scheduled_notifications (id, refType, refId, fireAt) VALUES ('n', 'CARE', 'c', 1)")
        db.close()
    }

    @Test
    fun migrate2To3KeepsDataAndAddsPhotos() {
        helper.createDatabase(2).apply {
            execSQL("INSERT INTO pets (id, name, species, createdAt, updatedAt) VALUES ('lilith', 'Lilith', 'CAT', 1, 1)")
            close()
        }
        val db = helper.runMigrationsAndValidate(3)
        db.prepare("SELECT COUNT(*) FROM pets").use { st ->
            st.step()
            assertEquals(1, st.getLong(0))
        }
        db.execSQL("INSERT INTO photos (id, path, width, height, createdAt) VALUES ('p', 'photos/p.jpg', 2000, 1500, 1)")
        db.close()
    }
}
