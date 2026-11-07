package com.krisna.stickerwamaker.ui.home

import android.graphics.Bitmap
import com.krisna.stickerwamaker.data.model.StickerItem
import com.krisna.stickerwamaker.data.model.StickerPack
import com.krisna.stickerwamaker.data.repository.StickerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeStickerRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStickerRepository()
        viewModel = HomeViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stickerPacks_initiallyEmpty() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stickerPacks.collect {}
        }
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.stickerPacks.value.isEmpty())
    }

    @Test
    fun createPack_addsNewPack() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stickerPacks.collect {}
        }
        var createdId = ""
        viewModel.createPack("Funny Memes", "John Doe") { id ->
            createdId = id
        }
        testScheduler.advanceUntilIdle()

        assertTrue(createdId.isNotBlank())
        val packs = viewModel.stickerPacks.value
        assertEquals(1, packs.size)
        assertEquals("Funny Memes", packs.first().name)
    }

    @Test
    fun deletePack_removesPack() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stickerPacks.collect {}
        }
        viewModel.createPack("To Delete", "John")
        testScheduler.advanceUntilIdle()
        val packId = viewModel.stickerPacks.value.first().identifier

        viewModel.deletePack(packId)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.stickerPacks.value.isEmpty())
    }
}

private class FakeStickerRepository : StickerRepository {
    private val packs = MutableStateFlow<List<StickerPack>>(emptyList())

    override fun getStickerPacks(): Flow<List<StickerPack>> = packs

    override fun getStickerPack(identifier: String): Flow<StickerPack?> {
        return packs.map { list -> list.find { it.identifier == identifier } }
    }

    override suspend fun createStickerPack(
        name: String,
        publisher: String,
        trayBitmap: Bitmap?
    ): Result<StickerPack> {
        val newPack = StickerPack(
            identifier = "id_${System.currentTimeMillis()}",
            name = name,
            publisher = publisher,
            trayImageFile = "tray.webp"
        )
        packs.value = packs.value + newPack
        return Result.success(newPack)
    }

    override suspend fun deleteStickerPack(identifier: String): Result<Unit> {
        packs.value = packs.value.filterNot { it.identifier == identifier }
        return Result.success(Unit)
    }

    override suspend fun addSticker(
        packIdentifier: String,
        bitmap: Bitmap,
        emojis: List<String>
    ): Result<StickerItem> {
        val item = StickerItem("sticker.webp")
        val pack = packs.value.find { it.identifier == packIdentifier }
        if (pack != null) {
            val updated = pack.copy(stickers = pack.stickers + item)
            packs.value = packs.value.map { if (it.identifier == packIdentifier) updated else it }
        }
        return Result.success(item)
    }

    override suspend fun removeSticker(packIdentifier: String, fileName: String): Result<Unit> {
        val pack = packs.value.find { it.identifier == packIdentifier }
        if (pack != null) {
            val updated = pack.copy(stickers = pack.stickers.filterNot { it.fileName == fileName })
            packs.value = packs.value.map { if (it.identifier == packIdentifier) updated else it }
        }
        return Result.success(Unit)
    }

    override suspend fun updateTrayIcon(packIdentifier: String, bitmap: Bitmap): Result<Unit> = Result.success(Unit)

    override fun getPackFile(packIdentifier: String, fileName: String): File? = null

    override fun getAllPacksDirect(): List<StickerPack> = packs.value

    override fun getPackDirect(identifier: String): StickerPack? = packs.value.find { it.identifier == identifier }
}
