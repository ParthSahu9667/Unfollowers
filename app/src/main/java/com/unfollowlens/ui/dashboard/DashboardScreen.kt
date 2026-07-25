package com.unfollowlens.ui.dashboard

import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unfollowlens.ui.components.*
import com.unfollowlens.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToList: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importZip(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        // Header
        Text("Unfollow Lens", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
        if (state.lastSyncedAt != null) {
            Text(
                "Last synced ${DateUtils.getRelativeTimeSpanString(state.lastSyncedAt!!)}",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Import button
        NeuCard(
            onClick = { filePicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) },
            accentColor = AccentPrimary, modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.FileUpload, "Import", tint = AccentPrimary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(if (state.hasData) "Import New Snapshot" else "Import Your First Snapshot",
                        style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Select your Instagram data export .zip",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        // Import status
        if (state.isImporting) {
            Spacer(Modifier.height(16.dp))
            ShimmerList(itemCount = 4)
        }

        state.importError?.let { error ->
            Spacer(Modifier.height(12.dp))
            NeuCard(accentColor = AccentNotBack, modifier = Modifier.fillMaxWidth()) {
                Text(error, style = MaterialTheme.typography.bodyMedium, color = AccentNotBack)
            }
        }

        if (!state.hasData && !state.isImporting) {
            Spacer(Modifier.height(48.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No data yet", style = MaterialTheme.typography.headlineSmall, color = TextSecondary)
                Text("Import your Instagram export to get started",
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp))
            }
            return
        }

        if (state.hasData) {
            Spacer(Modifier.height(24.dp))

            // Stat grid — 2×2
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Followers", state.followerCount, Modifier.weight(1f),
                    delta = state.deltaFollowers, accentColor = AccentPrimary,
                    onClick = { onNavigateToList("all_followers") })
                StatCard("Following", state.followingCount, Modifier.weight(1f),
                    delta = state.deltaFollowing, accentColor = AccentFan,
                    onClick = { onNavigateToList("all_following") })
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Mutuals", state.mutualCount, Modifier.weight(1f),
                    accentColor = AccentMutual, onClick = { onNavigateToList("mutuals") })
                StatCard("Not Back", state.notFollowingBackCount, Modifier.weight(1f),
                    accentColor = AccentNotBack, onClick = { onNavigateToList("not_back") })
            }

            Spacer(Modifier.height(12.dp))

            // Fans card — full width
            StatCard("Fans", state.fansCount, Modifier.fillMaxWidth(),
                accentColor = AccentFan, onClick = { onNavigateToList("fans") })

            Spacer(Modifier.height(32.dp))
        }
    }
}
