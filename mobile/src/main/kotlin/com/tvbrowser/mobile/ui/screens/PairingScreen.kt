package com.tvbrowser.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tvbrowser.mobile.viewmodel.MobileViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.tvbrowser.mobile.viewmodel.ViewModelFactory
import timber.log.Timber
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun PairingScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MobileViewModel = viewModel(
        viewModelStoreOwner = LocalViewModelStoreOwner.current!!,
        factory = ViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var ipAddressInput by remember { mutableStateOf("") }
    var showCameraScanner by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraScanner = true
        }
    }

    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            try {
                viewModel.pairWithQRCode(result.contents)
                showCameraScanner = false
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse QR code")
            }
        }
    }

    val scanOptions = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt("Scan TV QR Code")
        setCameraId(0)
        setBeepEnabled(true)
        setOrientationLocked(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Pair with TV",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // QR Code Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Scan TV QR Code",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            barcodeLauncher.launch(scanOptions)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Camera")
                }
            }
        }

        // Launch scanner when permission is granted
        LaunchedEffect(showCameraScanner) {
            if (showCameraScanner) {
                barcodeLauncher.launch(scanOptions)
                showCameraScanner = false
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN Input Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Or Enter PIN from TV",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ipAddressInput,
                    onValueChange = { ipAddressInput = it },
                    label = { Text("TV IP Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it.take(4) },
                    label = { Text("4-Digit PIN") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.pairWithPin(pinInput, ipAddressInput) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pinInput.length == 4 && ipAddressInput.isNotEmpty() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Connect")
                    }
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
