package com.northlight.feature.player

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object VlcPlayerHelper {

    const val VLC_PACKAGE_NAME = "org.videolan.vlc"

    /**
     * Checks if VLC is installed on this Android TV / device.
     */
    fun isVlcInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(VLC_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Creates an Intent to stream the video URL directly in VLC.
     */
    fun createVlcIntent(streamUrl: String, title: String): Intent {
        val uri = Uri.parse(streamUrl)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            setPackage(VLC_PACKAGE_NAME)
            putExtra("title", title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Creates an Intent to install VLC from Google Play Store on Android TV.
     */
    fun createPlayStoreIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$VLC_PACKAGE_NAME")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

