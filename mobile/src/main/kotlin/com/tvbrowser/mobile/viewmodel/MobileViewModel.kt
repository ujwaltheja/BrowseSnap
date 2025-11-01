package com.tvbrowser.mobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvbrowser.core.domain.models.SearchResult
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import com.tvbrowser.core.network.WebSocketManager
import com.tvbrowser.core.util.getDeviceId
import com.tvbrowser.core.util.isVideoFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID

data class MobileUiState(
    val isConnected: Boolean = false,
    val pairedTVs: List<String> = emptyList(),
    val searchResults: List<SearchResult> = emptyList(),
    val recentUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MobileViewModel(private val context: Context) : ViewModel() {
    private var webSocketManager: WebSocketManager? = null

    private val _uiState = MutableStateFlow(MobileUiState())
    val uiState: StateFlow<MobileUiState> = _uiState

    private val sharedPreferences = context.getSharedPreferences("tvbrowser_mobile", Context.MODE_PRIVATE)

    init {
        loadPairedTVs()
        loadRecentUrls()
    }

    fun pairWithQRCode(qrData: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val tvInfo = parsePairingQR(qrData)
                if (tvInfo != null) {
                    savePairedTV(tvInfo)
                    connectToTV(tvInfo)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Invalid QR code format",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to pair with QR code")
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun pairWithPin(pin: String, ipAddress: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val tvInfo = "$ipAddress"
                savePairedTV(tvInfo)
                connectToTV(tvInfo)
            } catch (e: Exception) {
                Timber.e(e, "Failed to pair with PIN")
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun connectToTV(tvAddress: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val wsUrl = "ws://$tvAddress:8888"
                webSocketManager = WebSocketManager(wsUrl, getAuthToken())

                if (webSocketManager?.connect() == true) {
                    webSocketManager?.sendCommand(
                        TVCommand.Register(
                            deviceId = UUID.randomUUID().toString(),
                            deviceName = android.os.Build.MODEL
                        )
                    )
                    listenForSocketEvents()
                    _uiState.value = _uiState.value.copy(
                        isConnected = true,
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to connect",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Connection failed")
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun sendUrlToTV(url: String) {
        viewModelScope.launch {
            try {
                val command = if (url.isVideoFile()) {
                    TVCommand.PlayVideo(url)
                } else {
                    TVCommand.OpenUrl(url)
                }

                if (webSocketManager?.sendCommand(command) == true) {
                    addRecentUrl(url)
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to send to TV"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to send URL")
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                webSocketManager?.sendCommand(TVCommand.Search(query))
            } catch (e: Exception) {
                Timber.e(e, "Search failed")
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun disconnect() {
        webSocketManager?.disconnect()
        _uiState.value = _uiState.value.copy(isConnected = false)
    }

    private fun parsePairingQR(qrData: String): String? {
        return try {
            val json = JSONObject(qrData)
            json.getString("ip")
        } catch (e: Exception) {
            Timber.e(e, "Could not parse QR data as JSON.")
            null
        }
    }

    private fun savePairedTV(tvAddress: String) {
        val pairedTVs = sharedPreferences.getStringSet("paired_tvs", mutableSetOf())?.toMutableList() ?: mutableListOf()
        if (!pairedTVs.contains(tvAddress)) {
            pairedTVs.add(tvAddress)
            sharedPreferences.edit().putStringSet("paired_tvs", pairedTVs.toSet()).apply()
        }
        loadPairedTVs()
    }

    private fun loadPairedTVs() {
        val pairedTVs = sharedPreferences.getStringSet("paired_tvs", emptySet())?.toList() ?: emptyList()
        _uiState.value = _uiState.value.copy(pairedTVs = pairedTVs)
    }

    private fun addRecentUrl(url: String) {
        val recentUrls = sharedPreferences.getStringSet("recent_urls", mutableSetOf())?.toMutableList() ?: mutableListOf()
        recentUrls.remove(url)
        recentUrls.add(0, url)
        if (recentUrls.size > 10) {
            recentUrls.removeAt(recentUrls.size - 1)
        }
        sharedPreferences.edit().putStringSet("recent_urls", recentUrls.toSet()).apply()
        loadRecentUrls()
    }

    private fun loadRecentUrls() {
        val recentUrls = sharedPreferences.getStringSet("recent_urls", emptySet())?.toList() ?: emptyList()
        _uiState.value = _uiState.value.copy(recentUrls = recentUrls)
    }

    private fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    private fun listenForSocketEvents() {
        webSocketManager?.commandResponse?.onEach { response ->
            when (response) {
                is TVResponse.SearchResults -> {
                    _uiState.value = _uiState.value.copy(
                        searchResults = response.results,
                        isLoading = false
                    )
                }
                is TVResponse.NowPlaying -> {
                    // Handle NowPlaying event
                }
                is TVResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message,
                        isLoading = false
                    )
                }
            }
        }?.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun sendCommand(command: TVCommand) {
        viewModelScope.launch {
            webSocketManager?.sendCommand(command)
        }
    }
}
