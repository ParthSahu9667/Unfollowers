package com.unfollowlens.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfollowlens.data.repository.SnapshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val snapshotCount: Int = 0,
    val showClearConfirm: Boolean = false,
    val reminderDays: Int = 14,
    val cleared: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SnapshotRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { _uiState.update { it.copy(snapshotCount = repository.getSnapshotCount()) } } }

    fun showClearConfirm() { _uiState.update { it.copy(showClearConfirm = true) } }
    fun dismissClearConfirm() { _uiState.update { it.copy(showClearConfirm = false) } }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.update { it.copy(showClearConfirm = false, snapshotCount = 0, cleared = true) }
        }
    }
}
