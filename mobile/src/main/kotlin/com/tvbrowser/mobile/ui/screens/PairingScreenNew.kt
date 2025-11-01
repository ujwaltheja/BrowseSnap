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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import com.tvbrowser.mobile.data.entity.PairedTV
import com.tvbrowser.mobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreenNew(
    viewModel: MainViewModel
) {
    val pairedTVs by viewModel.pairedTVs.collectAsState()
    var showManualPairingDialog by remember { mutableStateOf(false) }

    val qrScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        result.contents?.let { qrContent ->
            // Parse QR code content
            // Expected format: "browsesnap://pair?ip=192.168.1.100&pin=1234&name=Living Room TV"
            parseQRCode(qrContent)?.let { pairingInfo ->
                viewModel.addPairedTV(
                    ipAddress = pairingInfo.ip,
                    pin = pairingInfo.pin,
                    deviceName = pairingInfo.name
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paired Devices") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showManualPairingDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add TV") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Pairing Methods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val options = ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Scan QR Code from TV")
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                        }
                        qrScanLauncher.launch(options)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR")
                }

                Button(
                    onClick = { showManualPairingDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manual")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Paired TVs List
            Text(
                text = "Paired TVs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (pairedTVs.isEmpty()) {
                EmptyStateView(
                    message = "No paired TVs. Add one to get started!",
                    icon = Icons.Default.Tv
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pairedTVs) { tv ->
                        PairedTVCard(
                            tv = tv,
                            onConnect = { viewModel.selectTV(tv) },
                            onDelete = { viewModel.removePairedTV(tv) }
                        )
                    }
                }
            }
        }
    }

    if (showManualPairingDialog) {
        ManualPairingDialog(
            onDismiss = { showManualPairingDialog = false },
            onPair = { ip, pin, name ->
                viewModel.addPairedTV(ip, pin, name)
                showManualPairingDialog = false
            }
        )
    }
}

@Composable
fun PairedTVCard(
    tv: PairedTV,
    onConnect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tv.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tv.ipAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onConnect) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = "Connect",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove TV?") },
            text = { Text("Are you sure you want to remove ${tv.deviceName}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPairingDialog(
    onDismiss: () -> Unit,
    onPair: (String, String, String) -> Unit
) {
    var ipAddress by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("My TV") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pair TV Manually") },
        text = {
            Column {
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("TV Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Address") },
                    placeholder = { Text("192.168.1.100") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN") },
                    placeholder = { Text("1234") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPair(ipAddress, pin, deviceName) },
                enabled = ipAddress.isNotEmpty() && pin.isNotEmpty()
            ) {
                Text("Pair")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class PairingInfo(
    val ip: String,
    val pin: String,
    val name: String
)

fun parseQRCode(content: String): PairingInfo? {
    return try {
        // Parse browsesnap://pair?ip=192.168.1.100&pin=1234&name=Living Room TV
        if (!content.startsWith("browsesnap://pair")) return null

        val uri = android.net.Uri.parse(content)
        val ip = uri.getQueryParameter("ip") ?: return null
        val pin = uri.getQueryParameter("pin") ?: return null
        val name = uri.getQueryParameter("name") ?: "TV"

        PairingInfo(ip, pin, name)
    } catch (e: Exception) {
        null
    }
}
