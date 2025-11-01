package com.tvbrowser.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tvbrowser.tv.ui.screens.BrowserScreen
import com.tvbrowser.tv.ui.screens.PairingScreen
import com.tvbrowser.tv.ui.screens.VideoPlayerScreen
import com.tvbrowser.tv.ui.theme.TVBrowserTheme
import com.tvbrowser.tv.viewmodel.TVViewModel
import com.tvbrowser.tv.viewmodel.ViewModelFactory

class TVMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TVBrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: TVViewModel = ViewModelProvider(this, ViewModelFactory(this)).get(TVViewModel::class.java)
                    val navigation by viewModel.navigation.collectAsState()

                    LaunchedEffect(navigation) {
                        navigation?.let {
                            navController.navigate(it)
                            viewModel.onNavigationComplete()
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "pairing"
                    ) {
                        composable("pairing") {
                            PairingScreen(navController, viewModel)
                        }
                        composable("browser") {
                            BrowserScreen(navController, viewModel)
                        }
                        composable("player?url={url}") { backStackEntry ->
                            val url = backStackEntry.arguments?.getString("url") ?: ""
                            VideoPlayerScreen(navController, url, viewModel)
                        }
                    }
                }
            }
        }
    }
}
