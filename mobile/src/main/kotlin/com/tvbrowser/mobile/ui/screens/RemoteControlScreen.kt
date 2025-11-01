package com.tvbrowser.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tvbrowser.core.domain.models.TVCommand
import com.tvbrowser.mobile.viewmodel.MobileViewModel
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun RemoteControlScreen(
    navController: NavController,
    viewModel: MobileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val volumeLevel = remember { mutableStateOf(50f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Remote Control",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        if (!uiState.isConnected) {
            Text(
                text = "Not connected to TV",
                color = MaterialTheme.colorScheme.error
            )
            return@Column
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Navigation", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RemoteButton(
                    icon = Icons.Default.ArrowBack,
                    label = "Back",
                    onClick = {
                        viewModel.sendCommand(TVCommand.NavigateBack)
                    }
                )
                RemoteButton(
                    icon = Icons.Default.ArrowForward,
                    label = "Forward",
                    onClick = {
                        viewModel.sendCommand(TVCommand.NavigateForward)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Playback", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RemoteButton(
                    icon = Icons.Default.PlayArrow,
                    label = "Play",
                    onClick = {
                        viewModel.sendCommand(TVCommand.Resume)
                    }
                )
                RemoteButton(
                    icon = Icons.Default.PlayArrow, // Using PlayArrow, will add label
                    label = "Pause",
                    onClick = {
                        viewModel.sendCommand(TVCommand.Pause)
                    }
                )
                RemoteButton(
                    icon = Icons.Default.Close,
                    label = "Stop",
                    onClick = {
                        viewModel.sendCommand(TVCommand.Stop)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Volume", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.width(24.dp)
                )
                Slider(
                    value = volumeLevel.value,
                    onValueChange = { newVolume ->
                        volumeLevel.value = newVolume
                        viewModel.sendCommand(TVCommand.SetVolume(newVolume.toInt()))
                    },
                    valueRange = 0f..100f,
                    steps = 9,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.width(24.dp)
                )
                Text(
                    text = "${volumeLevel.value.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}

@Composable
fun RemoteButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(50.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
