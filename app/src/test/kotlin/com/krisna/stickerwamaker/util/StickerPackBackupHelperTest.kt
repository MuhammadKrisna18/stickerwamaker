package com.krisna.stickerwamaker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class StickerPackBackupHelperTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun zipCreationAndExtraction_preservesContent() {
        val sourceDir = tempFolder.newFolder("pack_dir")
        val metaFile = File(sourceDir, "metadata.json")
        metaFile.writeText("{\"identifier\":\"test-123\",\"name\":\"My Pack\"}")

        val dummySticker = File(sourceDir, "sticker_1.webp")
        dummySticker.writeBytes(byteArrayOf(1, 2, 3, 4, 5))

        val zipFile = File(tempFolder.root, "backup.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            sourceDir.listFiles()?.forEach { file ->
                zos.putNextEntry(ZipEntry(file.name))
                FileInputStream(file).use { fis -> fis.copyTo(zos) }
                zos.closeEntry()
            }
        }

        assertTrue(zipFile.exists())
        assertTrue(zipFile.length() > 0)

        val extractedFiles = mutableMapOf<String, ByteArray>()
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                extractedFiles[entry.name] = zis.readBytes()
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        assertEquals(2, extractedFiles.size)
        assertTrue(extractedFiles.containsKey("metadata.json"))
        assertTrue(extractedFiles.containsKey("sticker_1.webp"))
        assertEquals("{\"identifier\":\"test-123\",\"name\":\"My Pack\"}", String(extractedFiles["metadata.json"]!!))
    }
}

