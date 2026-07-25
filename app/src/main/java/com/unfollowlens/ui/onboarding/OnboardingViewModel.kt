package com.unfollowlens.ui.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfollowlens.data.repository.SnapshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val hasCheckedData: Boolean = false,
    val needsOnboarding: Boolean = true,
    val isImporting: Boolean = false,
    val importError: String? = null,
    val importSuccess: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: SnapshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        checkData()
    }

    private fun checkData() {
        viewModelScope.launch {
            val count = repository.getSnapshotCount()
            _uiState.update { 
                it.copy(
                    hasCheckedData = true,
                    needsOnboarding = count == 0
                )
            }
        }
    }

    fun importZip(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null, importSuccess = false) }
            val result = repository.importFromZip(uri)
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(isImporting = false, importSuccess = true, needsOnboarding = false)
                    }
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(isImporting = false, importError = e.message ?: "Failed to parse data. Make sure it's the correct Instagram export zip.")
                    }
                }
            )
        }
    }
}
