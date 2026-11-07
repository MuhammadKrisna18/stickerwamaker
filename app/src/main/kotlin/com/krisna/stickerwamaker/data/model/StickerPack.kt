package com.krisna.stickerwamaker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StickerPack(
    val identifier: String,
    val name: String,
    val publisher: String,
    val trayImageFile: String,
    val publisherEmail: String? = null,
    val publisherWebsite: String? = null,
    val privacyPolicyWebsite: String? = null,
    val licenseAgreementWebsite: String? = null,
    val imageDataVersion: String = "1",
    val avoidCache: Boolean = false,
    val animatedStickerPack: Boolean = false,
    val stickers: List<StickerItem> = emptyList()
) {
    val canExportToWhatsApp: Boolean
        get() = stickers.size in 3..30 && trayImageFile.isNotBlank()
}

