package com.krisna.stickerwamaker.data.model

object StickerPackValidator {
    const val MIN_STICKERS = 3
    const val MAX_STICKERS = 30
    const val MAX_EMOJIS_PER_STICKER = 3
    const val STICKER_SIZE_PX = 512
    const val TRAY_SIZE_PX = 96
    const val MAX_STATIC_FILE_SIZE_BYTES = 100 * 1024
    const val MAX_TRAY_FILE_SIZE_BYTES = 50 * 1024

    fun validatePack(pack: StickerPack): Result<Unit> {
        if (pack.identifier.isBlank()) {
            return Result.failure(IllegalArgumentException("Identifier cannot be empty"))
        }
        if (pack.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Pack name cannot be empty"))
        }
        if (pack.publisher.isBlank()) {
            return Result.failure(IllegalArgumentException("Publisher cannot be empty"))
        }
        if (pack.trayImageFile.isBlank()) {
            return Result.failure(IllegalArgumentException("Tray image cannot be empty"))
        }
        if (pack.stickers.size < MIN_STICKERS) {
            return Result.failure(IllegalStateException("A pack must have at least $MIN_STICKERS stickers"))
        }
        if (pack.stickers.size > MAX_STICKERS) {
            return Result.failure(IllegalStateException("A pack cannot exceed $MAX_STICKERS stickers"))
        }
        return Result.success(Unit)
    }
}

