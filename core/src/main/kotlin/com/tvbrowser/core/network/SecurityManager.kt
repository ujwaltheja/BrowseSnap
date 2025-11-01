package com.tvbrowser.core.network

import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

class SecurityManager {
    fun generatePairingToken(): String {
        return UUID.randomUUID().toString()
    }

    fun generatePIN(): String {
        return (1000..9999).random().toString()
    }

    fun validateOrigin(origin: String, allowedOrigins: Set<String>): Boolean {
        return allowedOrigins.contains(origin)
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    fun validateToken(token: String, expectedToken: String): Boolean {
        return token == expectedToken
    }

    fun isValidUrl(urlString: String): Boolean {
        return try {
            val url = java.net.URL(urlString)
            url.protocol in listOf("http", "https")
        } catch (e: Exception) {
            false
        }
    }

    fun sanitizeUrl(urlString: String): String {
        return try {
            val url = java.net.URL(urlString)
            url.toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun isVideoUrl(urlString: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".m3u8", ".mpd")
        val videoMimes = listOf("video/", "stream", "playlist")
        return videoExtensions.any { urlString.lowercase().contains(it) } ||
               videoMimes.any { urlString.lowercase().contains(it) }
    }
}
