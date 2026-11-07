package com.krisna.stickerwamaker.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.krisna.stickerwamaker.data.model.StickerPack
import com.krisna.stickerwamaker.data.repository.StickerRepositoryImpl
import java.io.FileNotFoundException

class StickerContentProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private lateinit var authority: String

    override fun onCreate(): Boolean {
        val appContext = context ?: return false
        authority = "${appContext.packageName}.stickercontentprovider"

        uriMatcher.addURI(authority, METADATA, CODE_METADATA)
        uriMatcher.addURI(authority, "$METADATA/*", CODE_METADATA_WITH_ID)
        uriMatcher.addURI(authority, STICKER_PACKS, CODE_METADATA)
        uriMatcher.addURI(authority, "$STICKER_PACKS/*", CODE_METADATA_WITH_ID)
        uriMatcher.addURI(authority, "$STICKERS/*", CODE_STICKERS)
        uriMatcher.addURI(authority, "$STICKERS_ASSET/*/*", CODE_STICKERS_ASSET)

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val appContext = context ?: return null
        val repo = StickerRepositoryImpl.getInstance(appContext)

        return when (uriMatcher.match(uri)) {
            CODE_METADATA -> {
                val packs = repo.getAllPacksDirect().filter { it.canExportToWhatsApp }
                createStickerPackCursor(packs)
            }
            CODE_METADATA_WITH_ID -> {
                val identifier = uri.lastPathSegment ?: return null
                val pack = repo.getPackDirect(identifier)
                if (pack != null && pack.canExportToWhatsApp) {
                    createStickerPackCursor(listOf(pack))
                } else {
                    createStickerPackCursor(emptyList())
                }
            }
            CODE_STICKERS -> {
                val identifier = uri.lastPathSegment ?: return null
                val pack = repo.getPackDirect(identifier) ?: return null
                createStickersCursor(pack)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val appContext = context ?: throw FileNotFoundException("Context is null")
        val match = uriMatcher.match(uri)
        if (match == CODE_STICKERS_ASSET) {
            val segments = uri.pathSegments
            if (segments.size >= 3) {
                val identifier = segments[1]
                val fileName = segments[2]
                val repo = StickerRepositoryImpl.getInstance(appContext)
                val file = repo.getPackFile(identifier, fileName)
                    ?: throw FileNotFoundException("File not found for uri: $uri")
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                return AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
            }
        }
        throw FileNotFoundException("Invalid URI: $uri")
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_METADATA -> "vnd.android.cursor.dir/vnd.$authority.$METADATA"
            CODE_METADATA_WITH_ID -> "vnd.android.cursor.item/vnd.$authority.$METADATA"
            CODE_STICKERS -> "vnd.android.cursor.dir/vnd.$authority.$STICKERS"
            CODE_STICKERS_ASSET -> {
                val lastSegment = uri.lastPathSegment ?: ""
                if (lastSegment.endsWith(".png", ignoreCase = true)) {
                    "image/png"
                } else {
                    "image/webp"
                }
            }
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    private fun createStickerPackCursor(packs: List<StickerPack>): Cursor {
        val cursor = MatrixCursor(STICKER_PACK_COLUMNS)
        for (pack in packs) {
            val trayIconFile = if (pack.trayImageFile.endsWith(".png", ignoreCase = true)) {
                pack.trayImageFile
            } else {
                "tray.png"
            }
            cursor.addRow(
                arrayOf<Any>(
                    pack.identifier,
                    pack.name,
                    pack.publisher,
                    trayIconFile,
                    "",
                    "",
                    pack.publisherEmail ?: "",
                    pack.publisherWebsite ?: "",
                    pack.privacyPolicyWebsite ?: "",
                    pack.licenseAgreementWebsite ?: "",
                    pack.imageDataVersion,
                    if (pack.avoidCache) 1 else 0,
                    if (pack.animatedStickerPack) 1 else 0
                )
            )
        }
        return cursor
    }

    private fun createStickersCursor(pack: StickerPack): Cursor {
        val cursor = MatrixCursor(STICKER_COLUMNS)
        for (sticker in pack.stickers) {
            val emojis = if (sticker.emojis.isNotEmpty()) {
                sticker.emojis.joinToString(",")
            } else {
                "😀"
            }
            cursor.addRow(
                arrayOf<Any>(
                    sticker.fileName,
                    emojis,
                    sticker.accessibilityText ?: ""
                )
            )
        }
        return cursor
    }

    companion object {
        private const val METADATA = "metadata"
        private const val STICKER_PACKS = "sticker_packs"
        private const val STICKERS = "stickers"
        private const val STICKERS_ASSET = "stickers_asset"

        private const val CODE_METADATA = 1
        private const val CODE_METADATA_WITH_ID = 2
        private const val CODE_STICKERS = 3
        private const val CODE_STICKERS_ASSET = 4

        private val STICKER_PACK_COLUMNS = arrayOf(
            "sticker_pack_identifier",
            "sticker_pack_name",
            "sticker_pack_publisher",
            "sticker_pack_icon",
            "android_play_store_link",
            "ios_app_store_link",
            "sticker_pack_publisher_email",
            "sticker_pack_publisher_website",
            "sticker_pack_privacy_policy_website",
            "sticker_pack_license_agreement_website",
            "image_data_version",
            "whatsapp_will_not_cache_stickers",
            "animated_sticker_pack"
        )

        private val STICKER_COLUMNS = arrayOf(
            "sticker_file_name",
            "sticker_emoji",
            "sticker_accessibility_text"
        )
    }
}
