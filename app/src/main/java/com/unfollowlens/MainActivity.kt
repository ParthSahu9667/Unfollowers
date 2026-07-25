package com.unfollowlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.unfollowlens.ui.navigation.AppNavigation
import com.unfollowlens.ui.onboarding.OnboardingScreen
import com.unfollowlens.ui.onboarding.OnboardingViewModel
import com.unfollowlens.ui.theme.UnfollowLensTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            UnfollowLensTheme {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                val onboardingState by onboardingViewModel.uiState.collectAsState()
                
                if (onboardingState.hasCheckedData) {
                    if (onboardingState.needsOnboarding) {
                        OnboardingScreen(onOnboardingComplete = { /* Re-composition will handle it */ })
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
