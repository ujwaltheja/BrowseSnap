package com.tvbrowser.core.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

fun String.isValidUrl(): Boolean {
    return try {
        Uri.parse(this)
        this.startsWith("http://") || this.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}

fun String.getMimeType(): String? {
    val uri = Uri.parse(this)
    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}

fun String.extractDomain(): String? {
    return try {
        Uri.parse(this).host
    } catch (e: Exception) {
        null
    }
}

fun String.isVideoFile(): Boolean {
    val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov", ".flv", ".m3u8", ".mpd")
    return videoExtensions.any { this.lowercase().endsWith(it) }
}

fun String.isImageFile(): Boolean {
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")
    return imageExtensions.any { this.lowercase().endsWith(it) }
}

fun Context.getDeviceId(): String {
    val sp = getSharedPreferences("tvbrowser_prefs", Context.MODE_PRIVATE)
    var deviceId = sp.getString("device_id", null)
    if (deviceId == null) {
        deviceId = java.util.UUID.randomUUID().toString()
        sp.edit().putString("device_id", deviceId).apply()
    }
    return deviceId
}
