package com.tvbrowser.tv.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.network.SecurityManager
import com.tvbrowser.tv.server.TVWebSocketServer
import java.net.InetAddress
import java.net.NetworkInterface

data class TVUiState(
    val isServerRunning: Boolean = false,
    val currentUrl: String? = null,
    val currentVideoUrl: String? = null,
    val tvName: String = "Android TV",
    val pairingPin: String = "",
    val connectedClients: Int = 0,
    val errorMessage: String? = null,
    val qrCodeData: String? = null
)

class TVViewModel(private val context: Context) : ViewModel() {
    private var webSocketServer: TVWebSocketServer? = null
    private val securityManager = SecurityManager()

    private val _uiState = MutableStateFlow(TVUiState())
    val uiState: StateFlow<TVUiState> = _uiState

    private val _navigation = MutableStateFlow<String?>(null)
    val navigation: StateFlow<String?> = _navigation

    init {
        generatePairingPin()
        startWebSocketServer()
    }

    private fun startWebSocketServer() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                webSocketServer = TVWebSocketServer(8888)
                webSocketServer?.onCommandReceived = { command ->
                    handleCommand(command)
                }
                webSocketServer?.start()
                _uiState.value = _uiState.value.copy(isServerRunning = true)
                Timber.d("WebSocket server started")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start WebSocket server")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to start server: ${e.message}"
                )
            }
        }
    }

    private fun generatePairingPin() {
        val pin = securityManager.generatePIN()
        _uiState.value = _uiState.value.copy(pairingPin = pin)

        val ipAddress = getLocalIpAddress() ?: "192.168.1.1"
        val qrData = """{"ip":"$ipAddress","port":8888,"pin":"$pin"}"""
        _uiState.value = _uiState.value.copy(qrCodeData = qrData)
    }

    private fun handleCommand(command: TVCommand) {
        when (command) {
            is TVCommand.OpenUrl -> {
                _uiState.value = _uiState.value.copy(currentUrl = command.url)
                _navigation.value = "browser"
                Timber.d("Opening URL: ${command.url}")
            }
            is TVCommand.PlayVideo -> {
                _uiState.value = _uiState.value.copy(currentVideoUrl = command.url)
                _navigation.value = "player?url=${command.url}"
                Timber.d("Playing video: ${command.url}")
            }
            is TVCommand.NavigateBack -> {
                _navigation.value = "pairing"
                Timber.d("Navigate back")
            }
            is TVCommand.Pause -> {
                Timber.d("Pause command")
            }
            is TVCommand.Resume -> {
                Timber.d("Resume command")
            }
            else -> {
                Timber.d("Unhandled command: $command")
            }
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting local IP")
        }
        return null
    }

    fun onNavigationComplete() {
        _navigation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        webSocketServer?.stop()
    }
}
