package com.tvbrowser.mobile.data.repository

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.network.TVWebSocketClient
import com.tvbrowser.mobile.data.dao.BrowsingHistoryDao
import com.tvbrowser.mobile.data.dao.PairedTVDao
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import com.tvbrowser.mobile.data.entity.PairedTV
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class TVRepository(
    private val pairedTVDao: PairedTVDao,
    private val browsingHistoryDao: BrowsingHistoryDao
) {
    private var webSocketClient: TVWebSocketClient? = null
    private var currentTVId: String? = null

    // Paired TVs
    fun getAllPairedTVs(): Flow<List<PairedTV>> = pairedTVDao.getAllPairedTVs()

    suspend fun addPairedTV(tv: PairedTV) {
        pairedTVDao.insert(tv)
        Timber.d("Added paired TV: ${tv.deviceName}")
    }

    suspend fun removePairedTV(tv: PairedTV) {
        pairedTVDao.delete(tv)
        if (currentTVId == tv.deviceId) {
            disconnect()
        }
        Timber.d("Removed paired TV: ${tv.deviceName}")
    }

    suspend fun updatePairedTV(tv: PairedTV) {
        pairedTVDao.update(tv)
    }

    // History
    fun getRecentHistory(limit: Int = 20): Flow<List<BrowsingHistory>> =
        browsingHistoryDao.getRecentHistory(limit)

    fun getAllHistory(): Flow<List<BrowsingHistory>> =
        browsingHistoryDao.getAllHistory()

    suspend fun addHistory(history: BrowsingHistory) {
        browsingHistoryDao.insert(history)
        Timber.d("Added history: ${history.url}")
    }

    suspend fun deleteHistory(history: BrowsingHistory) {
        browsingHistoryDao.delete(history)
    }

    suspend fun clearHistory() {
        browsingHistoryDao.clearAll()
    }

    // WebSocket Connection
    fun connectToTV(tv: PairedTV) {
        disconnect() // Disconnect from previous TV if any

        val serverUrl = "ws://${tv.ipAddress}:${tv.port}"
        webSocketClient = TVWebSocketClient(serverUrl).apply {
            connect()
        }
        currentTVId = tv.deviceId

        kotlinx.coroutines.GlobalScope.launch {
            pairedTVDao.updateLastConnected(tv.deviceId)
        }

        Timber.d("Connecting to TV: ${tv.deviceName} at $serverUrl")
    }

    fun disconnect() {
        webSocketClient?.disconnect()
        webSocketClient = null
        currentTVId = null
        Timber.d("Disconnected from TV")
    }

    fun getConnectionState() = webSocketClient?.connectionState

    fun getResponses() = webSocketClient?.responses

    // Send Commands
    suspend fun sendCommand(command: TVCommand): Boolean {
        val client = webSocketClient
        if (client == null) {
            Timber.w("No TV connected")
            return false
        }

        val success = client.sendCommand(command)

        // Save to history based on command type
        if (success) {
            when (command) {
                is TVCommand.OpenUrl -> {
                    addHistory(
                        BrowsingHistory(
                            url = command.url,
                            action = "open_url",
                            deviceId = currentTVId
                        )
                    )
                }
                is TVCommand.PlayVideo -> {
                    addHistory(
                        BrowsingHistory(
                            url = command.videoUrl,
                            title = command.title,
                            action = "play_video",
                            deviceId = currentTVId
                        )
                    )
                }
                else -> {} // Other commands don't need history
            }
        }

        return success
    }

    suspend fun openUrl(url: String) =
        sendCommand(TVCommand.OpenUrl(url))

    suspend fun playVideo(videoUrl: String, title: String? = null) =
        sendCommand(TVCommand.PlayVideo(videoUrl, title))

    suspend fun navigateBack() =
        sendCommand(TVCommand.NavigateBack())

    suspend fun navigateForward() =
        sendCommand(TVCommand.NavigateForward())

    suspend fun pause() =
        sendCommand(TVCommand.Pause())

    suspend fun resume() =
        sendCommand(TVCommand.Resume())

    suspend fun stop() =
        sendCommand(TVCommand.Stop())

    suspend fun setVolume(volume: Float) =
        sendCommand(TVCommand.SetVolume(volume))

    suspend fun seek(positionMs: Long) =
        sendCommand(TVCommand.Seek(positionMs))
}
