package com.unfollowlens.ui.history

import android.text.format.DateUtils
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unfollowlens.ui.components.*
import com.unfollowlens.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("History", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
        Spacer(Modifier.height(16.dp))

        if (state.snapshots.size >= 2) {
            // Sparkline chart
            SparklineChart(snapshots = state.snapshots, modifier = Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(20.dp))
        }

        if (state.isLoading) { ShimmerList(4) }
        else if (state.snapshots.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No snapshots yet", style = MaterialTheme.typography.headlineSmall, color = TextSecondary)
            }
        } else {
            Text("${state.snapshots.size} snapshots", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                itemsIndexed(state.snapshots, key = { _, s -> s.id }) { index, snapshot ->
                    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                    NeuCard(onClick = { viewModel.compareToPrevious(snapshot) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(dateFormat.format(Date(snapshot.importedAt)),
                                    style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                Text("${snapshot.followerCount} followers · ${snapshot.followingCount} following",
                                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            if (index < state.snapshots.size - 1) {
                                Icon(Icons.Default.CompareArrows, "Compare", tint = AccentPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Diff bottom sheet
    if (state.showDiffSheet && state.selectedDiff != null) {
        val diff = state.selectedDiff!!
        AlertDialog(
            onDismissRequest = viewModel::dismissDiff,
            containerColor = BgSurfaceElevated,
            title = { Text("Changes", color = TextPrimary) },
            text = {
                Column {
                    DiffRow("New followers", diff.newFollowers.size, AccentMutual)
                    DiffRow("Lost followers", diff.lostFollowers.size, AccentNotBack)
                    DiffRow("Started following", diff.newFollowing.size, AccentFan)
                    DiffRow("Unfollowed", diff.stoppedFollowing.size, AccentNotBack)
                    if (diff.lostFollowers.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Lost:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        diff.lostFollowers.take(20).forEach {
                            Text("  @$it", style = MaterialTheme.typography.bodySmall, color = AccentNotBack)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::dismissDiff) { Text("Close", color = AccentPrimary) } }
        )
    }
}

@Composable
private fun DiffRow(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text("$count", style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
fun SparklineChart(snapshots: List<com.unfollowlens.data.db.entities.Snapshot>, modifier: Modifier) {
    val sorted = snapshots.sortedBy { it.importedAt }
    if (sorted.size < 2) return

    Canvas(modifier.padding(8.dp)) {
        val maxVal = sorted.maxOf { it.followerCount }.toFloat().coerceAtLeast(1f)
        val minVal = sorted.minOf { it.followerCount }.toFloat()
        val range = (maxVal - minVal).coerceAtLeast(1f)
        val stepX = size.width / (sorted.size - 1).coerceAtLeast(1)
        val path = Path()

        sorted.forEachIndexed { i, s ->
            val x = i * stepX
            val y = size.height - ((s.followerCount - minVal) / range * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(AccentPrimary, 4.dp.toPx(), Offset(x, y))
        }
        drawPath(path, AccentPrimary.copy(alpha = 0.7f), style = Stroke(2.dp.toPx()))
    }
}
