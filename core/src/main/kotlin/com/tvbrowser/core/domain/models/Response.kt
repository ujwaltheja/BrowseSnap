package com.tvbrowser.core.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class TVResponse {
    abstract val timestamp: Long
    abstract val success: Boolean

    @Serializable
    data class CommandAck(
        val commandType: String,
        override val success: Boolean,
        val message: String? = null,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVResponse()

    @Serializable
    data class Error(
        val errorCode: String,
        val errorMessage: String,
        override val success: Boolean = false,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVResponse()

    @Serializable
    data class PairingSuccess(
        val deviceId: String,
        val deviceName: String,
        val authToken: String,
        override val success: Boolean = true,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVResponse()

    @Serializable
    data class StatusUpdate(
        val status: String,
        val details: Map<String, String>? = null,
        override val success: Boolean = true,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVResponse()

    @Serializable
    data class Pong(
        override val success: Boolean = true,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVResponse()

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }

        fun fromJson(jsonString: String): TVResponse? {
            return try {
                json.decodeFromString<TVResponse>(jsonString)
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to parse response: $jsonString")
                null
            }
        }

        fun toJson(response: TVResponse): String {
            return json.encodeToString(serializer(), response)
        }
    }
}
