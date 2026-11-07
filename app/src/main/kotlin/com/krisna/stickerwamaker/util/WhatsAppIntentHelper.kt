package com.krisna.stickerwamaker.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

enum class WhatsAppType(val title: String, val packageName: String) {
    REGULAR("WhatsApp", "com.whatsapp"),
    BUSINESS("WhatsApp Business", "com.whatsapp.w4b")
}

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

    fun getInstalledWhatsAppApps(context: Context): List<WhatsAppType> {
        val list = mutableListOf<WhatsAppType>()
        if (isAppInstalled(context, PACKAGE_WHATSAPP)) {
            list.add(WhatsAppType.REGULAR)
        }
        if (isAppInstalled(context, PACKAGE_WHATSAPP_BUSINESS)) {
            list.add(WhatsAppType.BUSINESS)
        }
        return list
    }

    fun isAnyWhatsAppInstalled(context: Context): Boolean {
        return getInstalledWhatsAppApps(context).isNotEmpty()
    }

    fun createAddPackIntent(
        context: Context,
        packIdentifier: String,
        packName: String,
        type: WhatsAppType = WhatsAppType.REGULAR
    ): Intent {
        val authority = "${context.packageName}.stickercontentprovider"

        return Intent().apply {
            action = ACTION_ENABLE_STICKER_PACK
            putExtra(EXTRA_STICKER_PACK_ID, packIdentifier)
            putExtra(EXTRA_STICKER_PACK_AUTHORITY, authority)
            putExtra(EXTRA_STICKER_PACK_NAME, packName)
            `package` = type.packageName
        }
    }
}
