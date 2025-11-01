package com.tvbrowser.tv.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tvbrowser.tv.viewmodel.TVViewModel
import timber.log.Timber

@Composable
fun BrowserScreen(
    navController: NavController,
    viewModel: TVViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoForward by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with navigation controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    webView?.goBack()
                    canGoBack = webView?.canGoBack() ?: false
                },
                enabled = canGoBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            IconButton(
                onClick = {
                    webView?.goForward()
                    canGoForward = webView?.canGoForward() ?: false
                },
                enabled = canGoForward
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward"
                )
            }

            IconButton(
                onClick = { webView?.reload() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload"
                )
            }

            Text(
                text = uiState.currentUrl ?: "Browser",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                maxLines = 1
            )

            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Exit Browser"
                )
            }
        }

        Divider()

        // WebView
        uiState.currentUrl?.let { url ->
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = false
                            setSupportZoom(false)
                            cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                canGoBack = view?.canGoBack() ?: false
                                canGoForward = view?.canGoForward() ?: false
                            }
                        }

                        loadUrl(url)
                    }.also { webView = it }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
