# BrowseSnap - Complete Production Code

## Root Configuration Files

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BrowseSnap"
include(":core")
include(":mobile")
include(":tv")
```

### build.gradle.kts (Root)
```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```

### gradle.properties
```properties
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# Android settings
android.useAndroidX=true
android.enableJetifier=true

# Kotlin settings
kotlin.code.style=official
kotlin.incremental=true
```

---

## Core Module

### core/build.gradle.kts
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tvbrowser.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    
    // WebSocket
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
```

### core/src/main/kotlin/com/tvbrowser/core/domain/models/Command.kt
```kotlin
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
```

### core/src/main/kotlin/com/tvbrowser/core/domain/models/Response.kt
```kotlin
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
```

### core/src/main/kotlin/com/tvbrowser/core/network/WebSocketClient.kt
```kotlin
package com.tvbrowser.core.network

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TVWebSocketClient(
    private val serverUrl: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _responses = MutableStateFlow<TVResponse?>(null)
    val responses: StateFlow<TVResponse?> = _responses.asStateFlow()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connected")
            _connectionState.value = ConnectionState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("Received message: $text")
            TVResponse.fromJson(text)?.let { response ->
                _responses.value = response
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closing: $code - $reason")
            _connectionState.value = ConnectionState.Disconnecting
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closed: $code - $reason")
            _connectionState.value = ConnectionState.Disconnected
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure")
            _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
        }
    }

    fun connect() {
        if (_connectionState.value is ConnectionState.Connected) {
            Timber.w("Already connected")
            return
        }

        _connectionState.value = ConnectionState.Connecting
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendCommand(command: TVCommand): Boolean {
        return try {
            val json = TVCommand.toJson(command)
            webSocket?.send(json) ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to send command")
            false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnecting : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}
```

### core/src/main/kotlin/com/tvbrowser/core/network/WebSocketServer.kt
```kotlin
package com.tvbrowser.core.network

import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.core.domain.models.TVResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import timber.log.Timber
import java.net.InetSocketAddress

class TVWebSocketServerImpl(
    port: Int = 8888
) : WebSocketServer(InetSocketAddress(port)) {

    private val _commands = MutableSharedFlow<TVCommand>(replay = 0)
    val commands: SharedFlow<TVCommand> = _commands.asSharedFlow()

    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount.asStateFlow()

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    init {
        isReuseAddr = true
        connectionLostTimeout = 30
    }

    override fun onStart() {
        Timber.d("WebSocket server started on port: $port")
        _serverState.value = ServerState.Running(port)
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Timber.d("New connection from: ${conn.remoteSocketAddress}")
        _connectionCount.value = connections.size
        
        // Send welcome message
        sendResponse(
            conn,
            TVResponse.StatusUpdate(
                status = "connected",
                details = mapOf("message" to "Connected to TV")
            )
        )
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Timber.d("Received message: $message")
        
        TVCommand.fromJson(message)?.let { command ->
            kotlinx.coroutines.GlobalScope.launch {
                _commands.emit(command)
            }
            
            // Send acknowledgment
            sendResponse(
                conn,
                TVResponse.CommandAck(
                    commandType = command::class.simpleName ?: "Unknown",
                    success = true,
                    message = "Command received"
                )
            )
        } ?: run {
            sendResponse(
                conn,
                TVResponse.Error(
                    errorCode = "INVALID_COMMAND",
                    errorMessage = "Failed to parse command"
                )
            )
        }
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Timber.d("Connection closed: $reason")
        _connectionCount.value = connections.size
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Timber.e(ex, "WebSocket server error")
        _serverState.value = ServerState.Error(ex.message ?: "Unknown error")
    }

    fun sendResponse(conn: WebSocket, response: TVResponse) {
        try {
            val json = TVResponse.toJson(response)
            conn.send(json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send response")
        }
    }

    fun broadcast(response: TVResponse) {
        try {
            val json = TVResponse.toJson(response)
            broadcast(json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to broadcast response")
        }
    }

    fun stopServer() {
        try {
            stop(1000)
            _serverState.value = ServerState.Stopped
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop server")
        }
    }

    sealed class ServerState {
        object Stopped : ServerState()
        data class Running(val port: Int) : ServerState()
        data class Error(val message: String) : ServerState()
    }
}
```

### core/src/main/kotlin/com/tvbrowser/core/util/Extensions.kt
```kotlin
package com.tvbrowser.core.util

import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

object NetworkUtils {
    
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    
                    if (!address.isLoopbackAddress && 
                        address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Failed to get IP address")
        }
        return null
    }

    fun generatePin(): String {
        return (1000..9999).random().toString()
    }

    fun generateDeviceId(): String {
        return UUID.randomUUID().toString()
    }
}

fun String.isValidUrl(): Boolean {
    return try {
        val url = java.net.URL(this)
        url.protocol == "http" || url.protocol == "https"
    } catch (e: Exception) {
        false
    }
}

fun String.isVideoUrl(): Boolean {
    val videoExtensions = listOf(".mp4", ".mkv", ".avi", ".mov", ".webm", ".m3u8")
    return videoExtensions.any { this.contains(it, ignoreCase = true) } ||
           this.contains("youtube.com", ignoreCase = true) ||
           this.contains("youtu.be", ignoreCase = true) ||
           this.contains("vimeo.com", ignoreCase = true)
}
```

---

## Mobile Module

### mobile/build.gradle.kts
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tvbrowser.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tvbrowser.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))

    // Kotlin
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    
    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // QR Code
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### mobile/src/main/AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />
    
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />

    <application
        android:name=".MobileApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.BrowseSnap"
        android:usesCleartextTraffic="true">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.BrowseSnap">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity
            android:name="com.journeyapps.barcodescanner.CaptureActivity"
            android:screenOrientation="portrait"
            tools:replace="screenOrientation" />
    </application>
</manifest>
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/MobileApplication.kt
```kotlin
package com.tvbrowser.mobile

import android.app.Application
import timber.log.Timber

class MobileApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("Mobile Application started")
    }
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/MainActivity.kt
```kotlin
package com.tvbrowser.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tvbrowser.mobile.ui.screens.*
import com.tvbrowser.mobile.ui.theme.BrowseSnapTheme
import com.tvbrowser.mobile.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BrowseSnapTheme {
                MobileApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileApp(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    val navigationItems = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("search", "Search", Icons.Default.Search),
        NavigationItem("history", "History", Icons.Default.History),
        NavigationItem("remote", "Remote", Icons.Default.ControlCamera),
        NavigationItem("devices", "Devices", Icons.Default.Tv)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BrowseSnap") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToPairing = { navController.navigate("devices") }
                )
            }
            
            composable("search") {
                SearchScreen(viewModel = viewModel)
            }
            
            composable("history") {
                HistoryScreen(viewModel = viewModel)
            }
            
            composable("remote") {
                RemoteControlScreen(viewModel = viewModel)
            }
            
            composable("devices") {
                PairingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
```

