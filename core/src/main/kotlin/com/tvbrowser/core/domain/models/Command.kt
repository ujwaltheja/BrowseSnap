package com.tvbrowser.core.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class TVCommand {
    abstract val timestamp: Long

    @Serializable
    data class OpenUrl(
        val url: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class PlayVideo(
        val videoUrl: String,
        val title: String? = null,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class NavigateBack(
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class NavigateForward(
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class Pause(
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class Resume(
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class Stop(
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class SetVolume(
        val volume: Float,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class Seek(
        val positionMs: Long,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class Register(
        val deviceId: String,
        val deviceName: String,
        val pin: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    @Serializable
    data class Ping(
        override val timestamp: Long = System.currentTimeMillis()
    ) : TVCommand()

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }

        fun fromJson(jsonString: String): TVCommand? {
            return try {
                json.decodeFromString<TVCommand>(jsonString)
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to parse command: $jsonString")
                null
            }
        }

        fun toJson(command: TVCommand): String {
            return json.encodeToString(serializer(), command)
        }
    }
}
