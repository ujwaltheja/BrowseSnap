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
fun SearchScreenNew(
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
