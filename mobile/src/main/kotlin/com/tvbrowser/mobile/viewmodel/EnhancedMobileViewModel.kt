package com.tvbrowser.mobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvbrowser.core.domain.models.SearchResult
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.util.isVideoFile
import com.tvbrowser.mobile.data.entity.HistoryEntity
import com.tvbrowser.mobile.data.entity.PairedTVEntity
import com.tvbrowser.mobile.data.repository.HistoryRepository
import com.tvbrowser.mobile.data.repository.PairedTVRepository
import com.tvbrowser.mobile.data.repository.TVConnectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID

/**
 * Enhanced Mobile ViewModel with Repository pattern
 * This is an improved version of MobileViewModel that uses repositories for data access
 */
class EnhancedMobileViewModel(
    private val context: Context,
    private val historyRepository: HistoryRepository,
    private val pairedTVRepository: PairedTVRepository,
    private val tvConnectionRepository: TVConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MobileUiState())
    val uiState: StateFlow<MobileUiState> = _uiState.asStateFlow()

    // Observe connection state from repository
    val connectionState = tvConnectionRepository.connectionState

    // Observe paired TVs from database
    val pairedTVs: Flow<List<PairedTVEntity>> = pairedTVRepository.getAllPairedTVsFlow()

    // Observe recent history from database
    val recentHistory: Flow<List<HistoryEntity>> = historyRepository.getRecentHistoryFlow(50)

    init {
        loadInitialData()
        observeConnectionState()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Load paired TVs
                val tvs = pairedTVRepository.getAllPairedTVs()
                _uiState.value = _uiState.value.copy(
                    pairedTVs = tvs.map { it.ipAddress }
                )

                // Load recent URLs from history
                val history = historyRepository.getRecentHistory(10)
                _uiState.value = _uiState.value.copy(
                    recentUrls = history.map { it.url }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading initial data")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            tvConnectionRepository.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isConnected = state is TVConnectionRepository.ConnectionState.Connected,
                    isLoading = state is TVConnectionRepository.ConnectionState.Connecting,
                    errorMessage = if (state is TVConnectionRepository.ConnectionState.Error) state.message else null
                )
            }
        }
    }

    fun pairWithQRCode(qrData: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val tvInfo = parsePairingQR(qrData)
                if (tvInfo != null) {
                    // Save to database
                    pairedTVRepository.addPairedTV(
                        deviceId = tvInfo["deviceId"] ?: UUID.randomUUID().toString(),
                        deviceName = tvInfo["deviceName"] ?: "TV",
                        ipAddress = tvInfo["ip"]!!,
                        port = tvInfo["port"]?.toIntOrNull() ?: 8888,
                        pin = tvInfo["pin"]
                    )

                    // Connect to TV
                    connectToTV(tvInfo["ip"]!!)
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

                // Save to database
                pairedTVRepository.addPairedTV(
                    deviceId = UUID.randomUUID().toString(),
                    deviceName = "TV ($ipAddress)",
                    ipAddress = ipAddress,
                    port = 8888,
                    pin = pin
                )

                // Connect to TV
                connectToTV(ipAddress)
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
                val wsUrl = "ws://$tvAddress:8888"
                val connected = tvConnectionRepository.connect(wsUrl, null)

                if (connected) {
                    // Update last connected timestamp
                    val tv = pairedTVRepository.getPairedTVByIp(tvAddress)
                    tv?.let {
                        pairedTVRepository.updateLastConnected(it.deviceId)
                    }

                    // Register device
                    tvConnectionRepository.sendCommand(
                        TVCommand.Register(
                            deviceId = UUID.randomUUID().toString(),
                            deviceName = android.os.Build.MODEL
                        )
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

                val sent = tvConnectionRepository.sendCommand(command)

                if (sent) {
                    // Save to history
                    historyRepository.addHistory(
                        url = url,
                        action = if (url.isVideoFile()) "play_video" else "open_url"
                    )
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

                // Search in local history first
                val historyResults = historyRepository.searchHistory(query)

                // Mock web search results - replace with actual API
                val webResults = listOf(
                    SearchResult(
                        title = "Search: $query on Google",
                        url = "https://google.com/search?q=$query"
                    ),
                    SearchResult(
                        title = "Search: $query on YouTube",
                        url = "https://youtube.com/results?search_query=$query"
                    )
                )

                _uiState.value = _uiState.value.copy(
                    searchResults = webResults,
                    isLoading = false
                )
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
        tvConnectionRepository.disconnect()
    }

    fun sendCommand(command: TVCommand) {
        viewModelScope.launch {
            tvConnectionRepository.sendCommand(command)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                historyRepository.clearAll()
                Timber.d("History cleared")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear history")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear history: ${e.message}"
                )
            }
        }
    }

    fun deletePairedTV(deviceId: String) {
        viewModelScope.launch {
            try {
                pairedTVRepository.deletePairedTVById(deviceId)
                Timber.d("Paired TV deleted: $deviceId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete paired TV")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete paired TV: ${e.message}"
                )
            }
        }
    }

    private fun parsePairingQR(qrData: String): Map<String, String>? {
        return try {
            val json = JSONObject(qrData)
            mapOf(
                "ip" to json.getString("ip"),
                "port" to json.optString("port", "8888"),
                "pin" to json.optString("pin", ""),
                "deviceId" to json.optString("deviceId", UUID.randomUUID().toString()),
                "deviceName" to json.optString("deviceName", "TV")
            )
        } catch (e: Exception) {
            Timber.e(e, "Could not parse QR data as JSON")
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
