package com.krisna.stickerwamaker.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.krisna.stickerwamaker.data.model.StickerItem
import com.krisna.stickerwamaker.data.model.StickerPack
import com.krisna.stickerwamaker.data.model.StickerPackValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class StickerRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StickerRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    private val rootDir = File(context.filesDir, "sticker_packs")
    private val mutex = Mutex()
    private val packsState = MutableStateFlow<List<StickerPack>>(emptyList())

    init {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        loadPacksFromDisk()
    }

    private fun loadPacksFromDisk() {
        val loaded = mutableListOf<StickerPack>()
        val dirs = rootDir.listFiles { file -> file.isDirectory } ?: emptyArray()
        for (dir in dirs) {
            val metaFile = File(dir, "metadata.json")
            if (metaFile.exists()) {
                try {
                    val pack = json.decodeFromString<StickerPack>(metaFile.readText())
                    loaded.add(pack)
                } catch (_: Exception) {
                }
            }
        }
        packsState.value = loaded.sortedBy { it.name }
    }

    override fun getStickerPacks(): Flow<List<StickerPack>> = packsState.asStateFlow()

    override fun getStickerPack(identifier: String): Flow<StickerPack?> {
        return packsState.map { list -> list.find { it.identifier == identifier } }
    }

    override suspend fun createStickerPack(
        name: String,
        publisher: String,
        trayBitmap: Bitmap?
    ): Result<StickerPack> = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val identifier = UUID.randomUUID().toString()
                val packDir = File(rootDir, identifier)
                if (!packDir.exists()) packDir.mkdirs()

                val trayFileName = "tray.webp"
                val trayFile = File(packDir, trayFileName)

                if (trayBitmap != null) {
                    val scaled = scaleBitmap(trayBitmap, StickerPackValidator.TRAY_SIZE_PX, StickerPackValidator.TRAY_SIZE_PX)
                    val bytes = compressWebp(scaled, StickerPackValidator.MAX_TRAY_FILE_SIZE_BYTES)
                    FileOutputStream(trayFile).use { it.write(bytes) }
                } else {
                    val placeholder = Bitmap.createBitmap(
                        StickerPackValidator.TRAY_SIZE_PX,
                        StickerPackValidator.TRAY_SIZE_PX,
                        Bitmap.Config.ARGB_8888
                    )
                    val bytes = compressWebp(placeholder, StickerPackValidator.MAX_TRAY_FILE_SIZE_BYTES)
                    FileOutputStream(trayFile).use { it.write(bytes) }
                }

                val newPack = StickerPack(
                    identifier = identifier,
                    name = name.trim(),
                    publisher = publisher.trim(),
                    trayImageFile = trayFileName,
                    stickers = emptyList()
                )

                savePackToDisk(newPack)
                packsState.value = (packsState.value + newPack).sortedBy { it.name }
                newPack
            }
        }
    }

    override suspend fun deleteStickerPack(identifier: String): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val packDir = File(rootDir, identifier)
                if (packDir.exists()) {
                    packDir.deleteRecursively()
                }
                packsState.value = packsState.value.filterNot { it.identifier == identifier }
                Unit
            }
        }
    }

    override suspend fun addSticker(
        packIdentifier: String,
        bitmap: Bitmap,
        emojis: List<String>
    ): Result<StickerItem> = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val currentPack = packsState.value.find { it.identifier == packIdentifier }
                    ?: throw IllegalArgumentException("Pack not found")

                if (currentPack.stickers.size >= StickerPackValidator.MAX_STICKERS) {
                    throw IllegalStateException("Maximum stickers limit reached")
                }

                val packDir = File(rootDir, packIdentifier)
                val fileName = "sticker_${System.currentTimeMillis()}.webp"
                val stickerFile = File(packDir, fileName)

                val scaled = scaleBitmap(bitmap, StickerPackValidator.STICKER_SIZE_PX, StickerPackValidator.STICKER_SIZE_PX)
                val bytes = compressWebp(scaled, StickerPackValidator.MAX_STATIC_FILE_SIZE_BYTES)
                FileOutputStream(stickerFile).use { it.write(bytes) }

                val newItem = StickerItem(
                    fileName = fileName,
                    emojis = emojis.take(StickerPackValidator.MAX_EMOJIS_PER_STICKER)
                )

                val updatedPack = currentPack.copy(
                    stickers = currentPack.stickers + newItem,
                    imageDataVersion = (currentPack.imageDataVersion.toIntOrNull()?.plus(1) ?: 1).toString()
                )

                savePackToDisk(updatedPack)
                packsState.value = packsState.value.map { if (it.identifier == packIdentifier) updatedPack else it }
                newItem
            }
        }
    }

    override suspend fun removeSticker(
        packIdentifier: String,
        fileName: String
    ): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val currentPack = packsState.value.find { it.identifier == packIdentifier }
                    ?: throw IllegalArgumentException("Pack not found")

                val packDir = File(rootDir, packIdentifier)
                val file = File(packDir, fileName)
                if (file.exists()) {
                    file.delete()
                }

                val updatedPack = currentPack.copy(
                    stickers = currentPack.stickers.filterNot { it.fileName == fileName },
                    imageDataVersion = (currentPack.imageDataVersion.toIntOrNull()?.plus(1) ?: 1).toString()
                )

                savePackToDisk(updatedPack)
                packsState.value = packsState.value.map { if (it.identifier == packIdentifier) updatedPack else it }
                Unit
            }
        }
    }

    override suspend fun updateTrayIcon(
        packIdentifier: String,
        bitmap: Bitmap
    ): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            runCatching {
                val currentPack = packsState.value.find { it.identifier == packIdentifier }
                    ?: throw IllegalArgumentException("Pack not found")

                val packDir = File(rootDir, packIdentifier)
                val trayFile = File(packDir, currentPack.trayImageFile)

                val scaled = scaleBitmap(bitmap, StickerPackValidator.TRAY_SIZE_PX, StickerPackValidator.TRAY_SIZE_PX)
                val bytes = compressWebp(scaled, StickerPackValidator.MAX_TRAY_FILE_SIZE_BYTES)
                FileOutputStream(trayFile).use { it.write(bytes) }

                val updatedPack = currentPack.copy(
                    imageDataVersion = (currentPack.imageDataVersion.toIntOrNull()?.plus(1) ?: 1).toString()
                )
                savePackToDisk(updatedPack)
                packsState.value = packsState.value.map { if (it.identifier == packIdentifier) updatedPack else it }
                Unit
            }
        }
    }

    override fun getPackFile(packIdentifier: String, fileName: String): File? {
        val file = File(File(rootDir, packIdentifier), fileName)
        return if (file.exists()) file else null
    }

    override fun getAllPacksDirect(): List<StickerPack> {
        val current = packsState.value
        return current.ifEmpty {
            loadPacksFromDisk()
            packsState.value
        }
    }

    override fun getPackDirect(identifier: String): StickerPack? {
        return getAllPacksDirect().find { it.identifier == identifier }
    }

    private fun savePackToDisk(pack: StickerPack) {
        val packDir = File(rootDir, pack.identifier)
        if (!packDir.exists()) packDir.mkdirs()
        val metaFile = File(packDir, "metadata.json")
        metaFile.writeText(json.encodeToString(pack))
    }

    private fun scaleBitmap(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        if (src.width == targetW && src.height == targetH) return src
        return Bitmap.createScaledBitmap(src, targetW, targetH, true)
    }

    private fun compressWebp(bitmap: Bitmap, maxBytes: Int): ByteArray {
        var quality = 90
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

        var stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)

        while (stream.size() > maxBytes && quality > 15) {
            quality -= 10
            stream = ByteArrayOutputStream()
            bitmap.compress(format, quality, stream)
        }
        return stream.toByteArray()
    }

    companion object {
        @Volatile
        private var instance: StickerRepositoryImpl? = null

        fun getInstance(context: Context): StickerRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: StickerRepositoryImpl(context.applicationContext).also { instance = it }
            }
        }
    }
}

