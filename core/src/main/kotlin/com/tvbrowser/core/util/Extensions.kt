package com.tvbrowser.core.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.net.NetworkInterface
import java.util.*

object NetworkUtils {

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses

                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()

                    if (!address.isLoopbackAddress &&
                        address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Failed to get IP address")
        }
        return null
    }

    fun generatePin(): String {
        return (1000..9999).random().toString()
    }

    fun generateDeviceId(): String {
        return UUID.randomUUID().toString()
    }
}

fun String.isValidUrl(): Boolean {
    return try {
        val url = java.net.URL(this)
        url.protocol == "http" || url.protocol == "https"
    } catch (e: Exception) {
        false
    }
}

fun String.isVideoUrl(): Boolean {
    val videoExtensions = listOf(".mp4", ".mkv", ".avi", ".mov", ".webm", ".m3u8")
    return videoExtensions.any { this.contains(it, ignoreCase = true) } ||
           this.contains("youtube.com", ignoreCase = true) ||
           this.contains("youtu.be", ignoreCase = true) ||
           this.contains("vimeo.com", ignoreCase = true)
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
        deviceId = UUID.randomUUID().toString()
        sp.edit().putString("device_id", deviceId).apply()
    }
    return deviceId
}
