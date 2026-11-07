package com.krisna.stickerwamaker

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data class PackDetailRoute(val packIdentifier: String) : NavKey
