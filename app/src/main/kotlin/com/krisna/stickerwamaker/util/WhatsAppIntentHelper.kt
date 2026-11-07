package com.krisna.stickerwamaker.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object WhatsAppIntentHelper {

    const val PACKAGE_WHATSAPP = "com.whatsapp"
    const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    private const val ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
    private const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    private const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    private const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isAnyWhatsAppInstalled(context: Context): Boolean {
        return isAppInstalled(context, PACKAGE_WHATSAPP) || isAppInstalled(context, PACKAGE_WHATSAPP_BUSINESS)
    }

    fun createAddPackIntent(
        context: Context,
        packIdentifier: String,
        packName: String,
        useBusiness: Boolean = false
    ): Intent {
        val authority = "${context.packageName}.stickercontentprovider"
        val targetPackage = if (useBusiness) PACKAGE_WHATSAPP_BUSINESS else PACKAGE_WHATSAPP

        return Intent().apply {
            action = ACTION_ENABLE_STICKER_PACK
            putExtra(EXTRA_STICKER_PACK_ID, packIdentifier)
            putExtra(EXTRA_STICKER_PACK_AUTHORITY, authority)
            putExtra(EXTRA_STICKER_PACK_NAME, packName)
            `package` = targetPackage
        }
    }
}

