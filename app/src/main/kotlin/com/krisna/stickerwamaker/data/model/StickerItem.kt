package com.krisna.stickerwamaker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StickerItem(
    val fileName: String,
    val emojis: List<String> = emptyList(),
    val accessibilityText: String? = null
)

