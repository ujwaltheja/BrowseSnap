package com.tvbrowser.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.network.TVWebSocketClient
import com.tvbrowser.core.util.NetworkUtils
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import com.tvbrowser.mobile.data.entity.PairedTV
import com.tvbrowser.mobile.data.repository.TVRepository
import com.tvbrowser.mobile.di.AppModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TVRepository

    init {
        AppModule.initialize(application)
        repository = AppModule.provideRepository()
    }

    // State flows
    val pairedTVs = repository.getAllPairedTVs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentHistory = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedTV = MutableStateFlow<PairedTV?>(null)
    val selectedTV: StateFlow<PairedTV?> = _selectedTV.asStateFlow()

    private val _connectionState = MutableStateFlow<TVWebSocketClient.ConnectionState>(
        TVWebSocketClient.ConnectionState.Disconnected
    )
    val connectionState: StateFlow<TVWebSocketClient.ConnectionState> =
        _connectionState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showPairingDialog = MutableStateFlow(false)
    val showPairingDialog: StateFlow<Boolean> = _showPairingDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Monitor connection state
        viewModelScope.launch {
            repository.getConnectionState()?.collect { state ->
                _connectionState.value = state
                when (state) {
                    is TVWebSocketClient.ConnectionState.Connected -> {
                        showToast("Connected to TV")
                    }
                    is TVWebSocketClient.ConnectionState.Error -> {
                        showToast("Connection error: ${state.message}")
                    }
                    is TVWebSocketClient.ConnectionState.Disconnected -> {
                        showToast("Disconnected from TV")
                    }
                    else -> {}
                }
            }
        }
    }

    // TV Connection
    fun selectTV(tv: PairedTV) {
        _selectedTV.value = tv
        repository.connectToTV(tv)
        Timber.d("Selected TV: ${tv.deviceName}")
    }

    fun disconnectTV() {
        repository.disconnect()
        _selectedTV.value = null
    }

    fun addPairedTV(ipAddress: String, pin: String, deviceName: String) {
        viewModelScope.launch {
            val deviceId = NetworkUtils.generateDeviceId()
            val tv = PairedTV(
                deviceId = deviceId,
                deviceName = deviceName,
                ipAddress = ipAddress,
                pin = pin
            )
            repository.addPairedTV(tv)
            selectTV(tv)
            _showPairingDialog.value = false
            Timber.d("Paired with TV: $deviceName")
        }
    }

    fun removePairedTV(tv: PairedTV) {
        viewModelScope.launch {
            repository.removePairedTV(tv)
            if (_selectedTV.value?.deviceId == tv.deviceId) {
                _selectedTV.value = null
            }
        }
    }

    // Commands
    fun sendUrl(url: String) {
        viewModelScope.launch {
            if (repository.openUrl(url)) {
                showToast("Sent URL to TV")
            } else {
                showToast("Failed to send URL")
            }
        }
    }

    fun playVideo(videoUrl: String, title: String? = null) {
        viewModelScope.launch {
            if (repository.playVideo(videoUrl, title)) {
                showToast("Playing video on TV")
            } else {
                showToast("Failed to play video")
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            repository.navigateBack()
        }
    }

    fun navigateForward() {
        viewModelScope.launch {
            repository.navigateForward()
        }
    }

    fun pause() {
        viewModelScope.launch {
            repository.pause()
        }
    }

    fun resume() {
        viewModelScope.launch {
            repository.resume()
        }
    }

    fun stop() {
        viewModelScope.launch {
            repository.stop()
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            repository.setVolume(volume)
        }
    }

    // History
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("History cleared")
        }
    }

    fun deleteHistoryItem(item: BrowsingHistory) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun replayHistory(item: BrowsingHistory) {
        when (item.action) {
            "open_url" -> sendUrl(item.url)
            "play_video" -> playVideo(item.url, item.title)
        }
    }

    // Search
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(query: String) {
        val url = if (query.startsWith("http://") || query.startsWith("https://")) {
            query
        } else if (query.contains(".")) {
            "https://$query"
        } else {
            "https://www.google.com/search?q=${query.replace(" ", "+")}"
        }
        sendUrl(url)
    }

    // UI State
    fun showPairingDialog() {
        _showPairingDialog.value = true
    }

    fun hidePairingDialog() {
        _showPairingDialog.value = false
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
