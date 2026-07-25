package com.unfollowlens.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unfollowlens.ui.components.NeuCard
import com.unfollowlens.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
        Spacer(Modifier.height(24.dp))

        // Storage section
        Text("Data & Storage", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        NeuCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Saved Snapshots", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    Text("Stored securely on your device", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Text("${state.snapshotCount}", style = MaterialTheme.typography.titleLarge, color = AccentPrimary)
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        NeuCard(onClick = viewModel::showClearConfirm, accentColor = AccentNotBack, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, "Delete", tint = AccentNotBack, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Clear All Data", style = MaterialTheme.typography.bodyLarge, color = AccentNotBack)
                    Text("Delete all imported snapshots", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // About Section
        Text("About", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        NeuCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text("Unfollow Lens v1.0", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("All data is processed locally on your device. No data is sent to external servers.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }

    if (state.showClearConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearConfirm,
            containerColor = BgSurfaceElevated,
            title = { Text("Clear All Data?", color = TextPrimary) },
            text = { Text("This will permanently delete all snapshots and history. This action cannot be undone.", color = TextSecondary) },
            confirmButton = { TextButton(onClick = viewModel::clearAllData) { Text("Delete", color = AccentNotBack) } },
            dismissButton = { TextButton(onClick = viewModel::dismissClearConfirm) { Text("Cancel", color = TextPrimary) } }
        )
    }
}
