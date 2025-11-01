# BrowseSnap - Mobile UI Screens

## Mobile UI Components

### mobile/src/main/kotlin/com/tvbrowser/mobile/ui/screens/HomeScreen.kt
```kotlin
package com.tvbrowser.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tvbrowser.core.network.TVWebSocketClient
import com.tvbrowser.mobile.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToPairing: () -> Unit
) {
    val pairedTVs by viewModel.pairedTVs.collectAsState()
    val selectedTV by viewModel.selectedTV.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Connection Status Card
        ConnectionStatusCard(
            selectedTV = selectedTV,
            connectionState = connectionState,
            onConnectClick = onNavigateToPairing
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Actions
        QuickActionsSection(
            onSearchClick = onNavigateToSearch,
            onPairClick = onNavigateToPairing,
            isConnected = connectionState is TVWebSocketClient.ConnectionState.Connected
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent History
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (recentHistory.isEmpty()) {
            EmptyStateView(
                message = "No recent activity",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentHistory.take(10)) { historyItem ->
                    HistoryItemCard(
                        item = historyItem,
                        onClick = { viewModel.replayHistory(historyItem) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    selectedTV: com.tvbrowser.mobile.data.entity.PairedTV?,
    connectionState: TVWebSocketClient.ConnectionState,
    onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is TVWebSocketClient.ConnectionState.Connected -> 
                    MaterialTheme.colorScheme.primaryContainer
                is TVWebSocketClient.ConnectionState.Error -> 
                    MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (connectionState) {
                        is TVWebSocketClient.ConnectionState.Connected -> "Connected"
                        is TVWebSocketClient.ConnectionState.Connecting -> "Connecting..."
                        is TVWebSocketClient.ConnectionState.Error -> "Error"
                        else -> "Not Connected"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (selectedTV != null) {
                    Text(
                        text = "${selectedTV.deviceName} (${selectedTV.ipAddress})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "No TV selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onConnectClick) {
                Icon(
                    imageVector = if (connectionState is TVWebSocketClient.ConnectionState.Connected) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Tv
                    },
                    contentDescription = "TV Status"
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onSearchClick: () -> Unit,
    onPairClick: () -> Unit,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.Search,
            label = "Search",
            onClick = onSearchClick,
            enabled = isConnected,
            modifier = Modifier.weight(1f)
        )
        
        QuickActionButton(
            icon = Icons.Default.Tv,
            label = "Pair TV",
            onClick = onPairClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label)
        }
    }
}

@Composable
fun HistoryItemCard(
    item: com.tvbrowser.mobile.data.entity.BrowsingHistory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.action == "play_video") 
                    Icons.Default.PlayCircle 
                else 
                    Icons.Default.Language,
                contentDescription = item.action,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: item.url,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Replay",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/ui/screens/SearchScreen.kt
```kotlin
package com.tvbrowser.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tvbrowser.mobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel
) {
    var searchText by remember { mutableStateOf("") }
    val selectedTV by viewModel.selectedTV.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Search & Browse",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter URL or search query") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (searchText.isNotEmpty()) {
                        viewModel.sendUrl(searchText)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = selectedTV != null && searchText.isNotEmpty()
            ) {
                Icon(Icons.Default.Language, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open URL")
            }
            
            Button(
                onClick = {
                    if (searchText.isNotEmpty()) {
                        viewModel.playVideo(searchText)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = selectedTV != null && searchText.isNotEmpty()
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play Video")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Links
        Text(
            text = "Quick Links",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val quickLinks = listOf(
            QuickLink("YouTube", "https://www.youtube.com", Icons.Default.VideoLibrary),
            QuickLink("Netflix", "https://www.netflix.com", Icons.Default.Movie),
            QuickLink("Google", "https://www.google.com", Icons.Default.Search),
            QuickLink("BBC News", "https://www.bbc.com/news", Icons.Default.Newspaper),
            QuickLink("Reddit", "https://www.reddit.com", Icons.Default.Forum),
            QuickLink("Twitch", "https://www.twitch.tv", Icons.Default.Stream)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickLinks) { link ->
                QuickLinkCard(
                    link = link,
                    onClick = { viewModel.sendUrl(link.url) },
                    enabled = selectedTV != null
                )
            }
        }
    }
}

data class QuickLink(
    val name: String,
    val url: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun QuickLinkCard(
    link: QuickLink,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = link.icon,
                    contentDescription = link.name,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = link.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### mobile/src/main/kotlin/com/tvbrowser/mobile/ui/screens/RemoteControlScreen.kt
```kotlin
package com.tvbrowser.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tvbrowser.core.network.TVWebSocketClient
import com.tvbrowser.mobile.viewmodel.MainViewModel

@Composable
fun RemoteControlScreen(
    viewModel: MainViewModel
) {
    val selectedTV by viewModel.selectedTV.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is TVWebSocketClient.ConnectionState.Connected
    
    var volume by remember { mutableFloatStateOf(0.5f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Remote Control",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (!isConnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Not connected to any TV",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation Controls
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RemoteButton(
                        icon = Icons.Default.ArrowBack,
                        label = "Back",
                        onClick = { viewModel.navigateBack() },
                        enabled = isConnected
                    )
                    
                    RemoteButton(
                        icon = Icons.Default.ArrowForward,
                        label = "Forward",
                        onClick = { viewModel.navigateForward() },
                        enabled = isConnected
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Playback Controls
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Playback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RemoteButton(
                        icon = Icons.Default.PlayArrow,
                        label = "Play",
                        onClick = { viewModel.resume() },
                        enabled = isConnected
                    )
                    
                    RemoteButton(
                        icon = Icons.Default.Pause,
                        label = "Pause",
                        onClick = { viewModel.pause() },
                        enabled = isConnected
                    )
                    
                    RemoteButton(
                        icon = Icons.Default.Stop,
                        label = "Stop",
                        onClick = { viewModel.stop() },
                        enabled = isConnected
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Volume Control
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Volume",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VolumeDown,
                        contentDescription = "Volume Down"
                    )
                    
                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                        onValueChangeFinished = {
                            viewModel.setVolume(volume)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isConnected
                    )
                    
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Volume Up"
                    )
                }
            }
        }
    }
}

@Composable
fun RemoteButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
```

