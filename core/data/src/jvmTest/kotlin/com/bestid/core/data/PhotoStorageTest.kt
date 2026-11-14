package com.bestid.core.data

import androidx.room.Room
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.db.configured
import com.bestid.core.data.repository.OkioPhotoFileStore
import com.bestid.core.data.repository.RoomPhotoRepository
import com.bestid.core.model.Photo
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class PhotoStorageTest {
    private val db = Room.inMemoryDatabaseBuilder<BestIdDatabase>().configured()
    private val fs = FakeFileSystem()
    private val files = OkioPhotoFileStore("/data/app", fs)
    private val photos = RoomPhotoRepository(db)

    @AfterTest
    fun close() {
        db.close()
        fs.checkNoOpenFiles()
    }

    @Test
    fun writesListsAndDeletesPhotos() = runTest {
        files.write("photos/a.jpg", byteArrayOf(1, 2, 3))
        files.write("photos/b.jpg", byteArrayOf(4))
        assertEquals(listOf("photos/a.jpg", "photos/b.jpg"), files.list().sorted())
        assertEquals(3L, fs.metadata("/data/app/photos/a.jpg".toPath()).size)
        assertEquals("/data/app/photos/a.jpg", files.absolutePath("photos/a.jpg"))

        files.delete("photos/a.jpg")
        files.delete("photos/no-existe.jpg")
        assertEquals(listOf("photos/b.jpg"), files.list())
    }

    @Test
    fun emptyDirectoryListsNothing() = runTest {
        assertTrue(files.list().isEmpty())
    }

    @Test
    fun photoRowsRoundTrip() = runTest {
        val t0 = Instant.fromEpochMilliseconds(1_700_000_000_000)
        val a = Photo("a", "photos/a.jpg", 2000, 1500, t0)
        val b = Photo("b", "photos/b.jpg", 1500, 2000, t0)
        photos.insert(a)
        photos.insert(b)
        assertEquals(listOf(b), photos.get(listOf("b", "x")))
        assertEquals(emptyList(), photos.get(emptyList()))
        photos.delete(listOf("a"))
        assertEquals(listOf(b), photos.getAll())
    }
}
