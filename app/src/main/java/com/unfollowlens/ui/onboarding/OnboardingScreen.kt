package com.unfollowlens.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unfollowlens.ui.components.NeuCard
import com.unfollowlens.ui.components.ShimmerList
import com.unfollowlens.ui.theme.*

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importZip(it) }
    }

    LaunchedEffect(state.hasCheckedData, state.needsOnboarding) {
        if (state.hasCheckedData && !state.needsOnboarding) {
            onOnboardingComplete()
        }
    }

    if (!state.hasCheckedData) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(48.dp))
        
        Text("Welcome to", style = MaterialTheme.typography.headlineMedium, color = TextSecondary)
        Text("Unfollow Lens", style = MaterialTheme.typography.displayMedium, color = TextPrimary)
        
        Spacer(Modifier.height(24.dp))
        
        NeuCard(accentColor = AccentMutual, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, "Privacy", tint = AccentMutual, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("100% Private & Offline", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("No login required. Your data never leaves your device.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("How it works:", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(Modifier.height(16.dp))
        
        InstructionStep(1, "Open Instagram app", "Settings > Accounts Center > Your information and permissions > Download your information")
        InstructionStep(2, "Request a download", "Choose 'Some of your information' > 'Followers and following'. Format must be JSON.")
        InstructionStep(3, "Wait for email & download", "Instagram will email you when your .zip file is ready.")
        InstructionStep(4, "Import it here", "Select the downloaded .zip file below.")
        
        Spacer(Modifier.height(32.dp))
        
        if (state.isImporting) {
            ShimmerList(itemCount = 2)
        } else {
            Button(
                onClick = { filePicker.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) {
                Icon(Icons.Default.FileUpload, "Import")
                Spacer(Modifier.width(8.dp))
                Text("Import .zip file", style = MaterialTheme.typography.labelLarge)
            }
        }
        
        state.importError?.let { error ->
            Spacer(Modifier.height(16.dp))
            NeuCard(accentColor = AccentNotBack, modifier = Modifier.fillMaxWidth()) {
                Text(error, style = MaterialTheme.typography.bodyMedium, color = AccentNotBack)
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun InstructionStep(stepNumber: Int, title: String, description: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier.size(28.dp).padding(top = 4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text("$stepNumber.", style = MaterialTheme.typography.titleMedium, color = AccentPrimary)
        }
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
