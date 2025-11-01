package com.tvbrowser.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed class TVCommand {
    @Serializable
    data class OpenUrl(val url: String) : TVCommand()

    @Serializable
    data class PlayVideo(
        val url: String,
        val title: String? = null,
        val startPosition: Long = 0
    ) : TVCommand()

    @Serializable
    object NavigateBack : TVCommand()

    @Serializable
    object NavigateForward : TVCommand()

    @Serializable
    object Pause : TVCommand()

    @Serializable
    object Resume : TVCommand()

    @Serializable
    data class Seek(val positionMs: Long) : TVCommand()

    @Serializable
    data class SetVolume(val level: Int) : TVCommand()

    @Serializable
    data class Register(val deviceId: String, val deviceName: String = "Mobile") : TVCommand()

    @Serializable
    object Stop : TVCommand()

    @Serializable
    data class SetSubtitles(val url: String?) : TVCommand()
}

@Serializable
sealed class TVResponse {
    @Serializable
    data class Status(
        val state: String,
        val currentUrl: String? = null,
        val playbackPosition: Long? = null,
        val duration: Long? = null
    ) : TVResponse()

    @Serializable
    data class Error(val message: String, val code: Int = 500) : TVResponse()

    @Serializable
    object Success : TVResponse()

    @Serializable
    data class PairingInfo(
        val token: String,
        val tvName: String,
        val ip: String,
        val port: Int
    ) : TVResponse()
}

@Serializable
data class TVPairingData(
    val ip: String,
    val port: Int,
    val token: String,
    val tvName: String = "Android TV"
)

@Serializable
data class SearchResult(
    val title: String,
    val url: String,
    val type: ContentType = ContentType.WEBPAGE,
    val thumbnail: String? = null
)

@Serializable
enum class ContentType {
    WEBPAGE, VIDEO, AUDIO, IMAGE
}
