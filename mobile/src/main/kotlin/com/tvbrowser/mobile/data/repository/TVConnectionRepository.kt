package com.tvbrowser.mobile.data.repository

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import com.tvbrowser.core.network.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing WebSocket connection to TV
 */
@Singleton
class TVConnectionRepository @Inject constructor() {

    private var webSocketManager: WebSocketManager? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _responses = MutableStateFlow<TVResponse?>(null)
    val responses: StateFlow<TVResponse?> = _responses

    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    /**
     * Connect to TV WebSocket server
     */
    suspend fun connect(wsUrl: String, authToken: String? = null): Boolean {
        return try {
            _connectionState.value = ConnectionState.Connecting
            webSocketManager = WebSocketManager(wsUrl, authToken)

            val connected = webSocketManager?.connect() ?: false

            if (connected) {
                _connectionState.value = ConnectionState.Connected
                Timber.d("Connected to TV at $wsUrl")

                // Collect responses
                webSocketManager?.commandResponse?.collect { response ->
                    _responses.value = response
                }
            } else {
                _connectionState.value = ConnectionState.Error("Failed to connect")
                Timber.e("Failed to connect to TV")
            }

            connected
        } catch (e: Exception) {
            Timber.e(e, "Connection error")
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            false
        }
    }

    /**
     * Send command to TV
     */
    suspend fun sendCommand(command: TVCommand): Boolean {
        return try {
            val result = webSocketManager?.sendCommand(command) ?: false
            if (!result) {
                Timber.e("Failed to send command: $command")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error sending command")
            false
        }
    }

    /**
     * Disconnect from TV
     */
    fun disconnect() {
        webSocketManager?.disconnect()
        webSocketManager = null
        _connectionState.value = ConnectionState.Disconnected
        Timber.d("Disconnected from TV")
    }

    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return _connectionState.value is ConnectionState.Connected
    }

    /**
     * Get current connection state
     */
    fun getConnectionState(): ConnectionState {
        return _connectionState.value
    }
}
