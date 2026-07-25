package com.unfollowlens.ui.lists

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unfollowlens.ui.components.*
import com.unfollowlens.ui.theme.*

@Composable
fun ListsScreen(
    initialCategory: String? = null,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(initialCategory) {
        initialCategory?.let { viewModel.setInitialCategory(it) }
    }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            viewModel.writeCsvToUri(context, uri)
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Lists", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            IconButton(onClick = { csvLauncher.launch("unfollow_lens_export.csv") }) {
                Icon(Icons.Default.Download, contentDescription = "Export to CSV", tint = AccentPrimary)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Segmented control
        SegmentedControl(
            items = listOf("Not Back", "Fans", "Mutuals", "All"),
            selectedIndex = when (state.selectedCategory) {
                ListCategory.NOT_BACK -> 0; ListCategory.FANS -> 1
                ListCategory.MUTUALS -> 2; else -> 3
            },
            onItemSelected = { index ->
                viewModel.selectCategory(
                    when (index) { 0 -> ListCategory.NOT_BACK; 1 -> ListCategory.FANS
                        2 -> ListCategory.MUTUALS; else -> ListCategory.ALL_FOLLOWERS }
                )
            }
        )

        Spacer(Modifier.height(16.dp))
        SearchBar(query = state.searchQuery, onQueryChange = viewModel::updateSearch)
        Spacer(Modifier.height(8.dp))

        // Count badge
        Text("${state.filteredUsers.size} users", style = MaterialTheme.typography.labelMedium,
            color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))

        if (state.isLoading) {
            ShimmerList()
        } else if (!state.hasData) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Import a snapshot first", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        } else if (state.filteredUsers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No users found", style = MaterialTheme.typography.headlineSmall, color = TextSecondary)
                    if (state.searchQuery.isNotEmpty())
                        Text("Try a different search", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)) {
                itemsIndexed(state.filteredUsers, key = { _, r -> r.id }) { index, record ->
                    val accentColor = when (state.selectedCategory) {
                        ListCategory.NOT_BACK -> AccentNotBack; ListCategory.FANS -> AccentFan
                        ListCategory.MUTUALS -> AccentMutual; else -> AccentPrimary
                    }
                    // Staggered spring entrance animation
                    val animatable = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 30L)
                        animatable.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
                    }
                    UserListItem(
                        username = record.username,
                        subtitle = record.profileUrl?.substringAfterLast("/")?.ifEmpty { null },
                        accentColor = accentColor,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://instagram.com/${record.username}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.graphicsLayer {
                            alpha = animatable.value
                            translationY = (1f - animatable.value) * 40f
                        }
                    )
                }
            }
        }
    }
}
