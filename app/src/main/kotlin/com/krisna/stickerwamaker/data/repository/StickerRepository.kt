package com.krisna.stickerwamaker.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.krisna.stickerwamaker.data.model.StickerItem
import com.krisna.stickerwamaker.data.model.StickerPack
import kotlinx.coroutines.flow.Flow
import java.io.File

interface StickerRepository {
    fun getStickerPacks(): Flow<List<StickerPack>>
    fun getStickerPack(identifier: String): Flow<StickerPack?>
    suspend fun createStickerPack(name: String, publisher: String, trayBitmap: Bitmap? = null): Result<StickerPack>
    suspend fun deleteStickerPack(identifier: String): Result<Unit>
    suspend fun addSticker(packIdentifier: String, bitmap: Bitmap, emojis: List<String> = emptyList()): Result<StickerItem>
    suspend fun removeSticker(packIdentifier: String, fileName: String): Result<Unit>
    suspend fun updateTrayIcon(packIdentifier: String, bitmap: Bitmap): Result<Unit>
    suspend fun importPackFromZip(zipUri: Uri): Result<StickerPack>
    fun getPackFile(packIdentifier: String, fileName: String): File?
    fun getAllPacksDirect(): List<StickerPack>
    fun getPackDirect(identifier: String): StickerPack?
}
