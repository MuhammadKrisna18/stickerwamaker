package com.krisna.stickerwamaker.updater

import com.krisna.stickerwamaker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AppUpdateChecker {

    private const val GITHUB_REPO = "MuhammadKrisna18/stickerwamaker"
    private const val API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    suspend fun checkForUpdate(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "StickerWAMaker-App")
            }

            if (connection.responseCode != 200) {
                throw IllegalStateException("GitHub API returned code: ${connection.responseCode}")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            val rawTag = json.optString("tag_name", "")
            val cleanTag = rawTag.removePrefix("v").trim()
            val releaseNotes = json.optString("body", "Bug fixes and improvements")

            var downloadUrl = ""
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()
            val isUpdateAvailable = isVersionGreater(cleanTag, currentVersion) && downloadUrl.isNotBlank()

            AppUpdateInfo(
                latestVersion = cleanTag,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                isUpdateAvailable = isUpdateAvailable
            )
        }
    }

    fun isVersionGreater(remote: String, current: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}

