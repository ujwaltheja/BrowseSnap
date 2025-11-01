package com.tvbrowser.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tvbrowser.mobile.ui.screens.HomeScreen
import com.tvbrowser.mobile.ui.screens.PairingScreen
import com.tvbrowser.mobile.ui.screens.SearchScreen
import com.tvbrowser.mobile.ui.screens.RemoteControlScreen
import com.tvbrowser.mobile.ui.theme.TVBrowserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TVBrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("pairing") {
                            PairingScreen(navController)
                        }
                        composable("search") {
                            SearchScreen(navController)
                        }
                        composable("remote") {
                            RemoteControlScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
