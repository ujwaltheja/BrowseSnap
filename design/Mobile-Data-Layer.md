# BrowseSnap - Mobile Module Complete Code (Part 2)

## Mobile Data Layer

### mobile/src/main/kotlin/com/tvbrowser/mobile/data/entity/BrowsingHistory.kt
```kotlin
package com.tvbrowser.mobile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browsing_history")
data class BrowsingHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
    val title: String? = null,
    val action: String, // "open_url" or "play_video"
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null,
    val deviceId: String? = null
)
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/data/entity/PairedTV.kt
```kotlin
package com.tvbrowser.mobile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_tvs")
data class PairedTV(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int = 8888,
    val pin: String? = null,
    val authToken: String? = null,
    val lastConnected: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/data/dao/BrowsingHistoryDao.kt
```kotlin
package com.tvbrowser.mobile.data.dao

import androidx.room.*
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowsingHistoryDao {
    
    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<BrowsingHistory>>
    
    @Query("SELECT * FROM browsing_history WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getHistoryByDevice(deviceId: String): Flow<List<BrowsingHistory>>
    
    @Query("SELECT * FROM browsing_history WHERE action = :action ORDER BY timestamp DESC LIMIT :limit")
    fun getHistoryByAction(action: String, limit: Int = 50): Flow<List<BrowsingHistory>>
    
    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 20): Flow<List<BrowsingHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: BrowsingHistory): Long
    
    @Delete
    suspend fun delete(history: BrowsingHistory)
    
    @Query("DELETE FROM browsing_history")
    suspend fun clearAll()
    
    @Query("DELETE FROM browsing_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/data/dao/PairedTVDao.kt
```kotlin
package com.tvbrowser.mobile.data.dao

import androidx.room.*
import com.tvbrowser.mobile.data.entity.PairedTV
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedTVDao {
    
    @Query("SELECT * FROM paired_tvs ORDER BY lastConnected DESC")
    fun getAllPairedTVs(): Flow<List<PairedTV>>
    
    @Query("SELECT * FROM paired_tvs WHERE deviceId = :deviceId")
    suspend fun getPairedTV(deviceId: String): PairedTV?
    
    @Query("SELECT * FROM paired_tvs WHERE ipAddress = :ipAddress")
    suspend fun getPairedTVByIp(ipAddress: String): PairedTV?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tv: PairedTV)
    
    @Update
    suspend fun update(tv: PairedTV)
    
    @Delete
    suspend fun delete(tv: PairedTV)
    
    @Query("DELETE FROM paired_tvs")
    suspend fun clearAll()
    
    @Query("UPDATE paired_tvs SET lastConnected = :timestamp WHERE deviceId = :deviceId")
    suspend fun updateLastConnected(deviceId: String, timestamp: Long = System.currentTimeMillis())
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/data/database/AppDatabase.kt
```kotlin
package com.tvbrowser.mobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvbrowser.mobile.data.dao.BrowsingHistoryDao
import com.tvbrowser.mobile.data.dao.PairedTVDao
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import com.tvbrowser.mobile.data.entity.PairedTV

@Database(
    entities = [BrowsingHistory::class, PairedTV::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun browsingHistoryDao(): BrowsingHistoryDao
    abstract fun pairedTVDao(): PairedTVDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "browsesnap_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/data/repository/TVRepository.kt
```kotlin
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
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/di/AppModule.kt
```kotlin
package com.tvbrowser.mobile.di

import android.content.Context
import com.tvbrowser.mobile.data.database.AppDatabase
import com.tvbrowser.mobile.data.repository.TVRepository

object AppModule {
    
    private lateinit var database: AppDatabase
    private lateinit var repository: TVRepository
    
    fun initialize(context: Context) {
        database = AppDatabase.getDatabase(context)
        repository = TVRepository(
            pairedTVDao = database.pairedTVDao(),
            browsingHistoryDao = database.browsingHistoryDao()
        )
    }
    
    fun provideRepository(): TVRepository {
        if (!::repository.isInitialized) {
            throw IllegalStateException("AppModule not initialized")
        }
        return repository
    }
    
    fun provideDatabase(): AppDatabase {
        if (!::database.isInitialized) {
            throw IllegalStateException("AppModule not initialized")
        }
        return database
    }
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/viewmodel/MainViewModel.kt
```kotlin
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
```

