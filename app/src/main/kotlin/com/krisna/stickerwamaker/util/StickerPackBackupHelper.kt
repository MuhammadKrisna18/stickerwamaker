package com.krisna.stickerwamaker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.krisna.stickerwamaker.data.repository.StickerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object StickerPackBackupHelper {

    suspend fun exportPackToZip(
        context: Context,
        packIdentifier: String,
        repository: StickerRepository
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val pack = repository.getPackDirect(packIdentifier)
                ?: throw IllegalArgumentException("Pack not found")

            val cacheDir = File(context.cacheDir, "packs").apply { if (!exists()) mkdirs() }
            val cleanName = pack.name.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val zipFile = File(cacheDir, "${cleanName}_stickerpack.zip")

            val packDir = File(File(context.filesDir, "sticker_packs"), packIdentifier)
            if (!packDir.exists() || !packDir.isDirectory) {
                throw IllegalStateException("Pack directory does not exist")
            }

            val files = packDir.listFiles() ?: emptyArray()

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in files) {
                    if (file.isFile) {
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        FileInputStream(file).use { fis ->
                            fis.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }

            zipFile
        }
    }

    fun createShareIntent(context: Context, file: File): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

