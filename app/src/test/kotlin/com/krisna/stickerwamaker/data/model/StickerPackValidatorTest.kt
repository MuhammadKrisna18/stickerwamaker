package com.krisna.stickerwamaker.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StickerPackValidatorTest {

    @Test
    fun validatePack_validPack_returnsSuccess() {
        val stickers = (1..5).map {
            StickerItem(fileName = "sticker_$it.webp", emojis = listOf("😀"))
        }
        val pack = StickerPack(
            identifier = "pack-1",
            name = "Test Pack",
            publisher = "Test Publisher",
            trayImageFile = "tray.webp",
            stickers = stickers
        )

        val result = StickerPackValidator.validatePack(pack)
        assertTrue(result.isSuccess)
    }

    @Test
    fun validatePack_lessThanMinStickers_returnsFailure() {
        val stickers = listOf(
            StickerItem(fileName = "sticker_1.webp"),
            StickerItem(fileName = "sticker_2.webp")
        )
        val pack = StickerPack(
            identifier = "pack-1",
            name = "Test Pack",
            publisher = "Test Publisher",
            trayImageFile = "tray.webp",
            stickers = stickers
        )

        val result = StickerPackValidator.validatePack(pack)
        assertFalse(result.isSuccess)
    }

    @Test
    fun validatePack_moreThanMaxStickers_returnsFailure() {
        val stickers = (1..31).map {
            StickerItem(fileName = "sticker_$it.webp")
        }
        val pack = StickerPack(
            identifier = "pack-1",
            name = "Test Pack",
            publisher = "Test Publisher",
            trayImageFile = "tray.webp",
            stickers = stickers
        )

        val result = StickerPackValidator.validatePack(pack)
        assertFalse(result.isSuccess)
    }

    @Test
    fun validatePack_emptyFields_returnsFailure() {
        val stickers = (1..3).map {
            StickerItem(fileName = "sticker_$it.webp")
        }
        val pack = StickerPack(
            identifier = "",
            name = "",
            publisher = "",
            trayImageFile = "",
            stickers = stickers
        )

        val result = StickerPackValidator.validatePack(pack)
        assertFalse(result.isSuccess)
    }
}

