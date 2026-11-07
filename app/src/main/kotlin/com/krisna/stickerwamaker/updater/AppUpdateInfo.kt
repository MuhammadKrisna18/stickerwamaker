package com.krisna.stickerwamaker.updater

data class AppUpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

