package com.unfollowlens.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfollowlens.data.db.entities.Snapshot
import com.unfollowlens.data.repository.SnapshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val snapshots: List<Snapshot> = emptyList(),
    val selectedDiff: SnapshotRepository.SnapshotDiff? = null,
    val selectedOldSnapshot: Snapshot? = null,
    val selectedNewSnapshot: Snapshot? = null,
    val showDiffSheet: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: SnapshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSnapshots().collect { snapshots ->
                _uiState.update { it.copy(isLoading = false, snapshots = snapshots) }
            }
        }
    }

    fun compareToPrevious(snapshot: Snapshot) {
        val snapshots = _uiState.value.snapshots
        val index = snapshots.indexOf(snapshot)
        if (index < 0 || index >= snapshots.size - 1) return
        val older = snapshots[index + 1]
        viewModelScope.launch {
            val diff = repository.computeDiff(older.id, snapshot.id)
            _uiState.update {
                it.copy(selectedDiff = diff, selectedOldSnapshot = older,
                    selectedNewSnapshot = snapshot, showDiffSheet = true)
            }
        }
    }

    fun dismissDiff() { _uiState.update { it.copy(showDiffSheet = false, selectedDiff = null) } }

    fun deleteSnapshot(snapshot: Snapshot) {
        viewModelScope.launch { repository.deleteSnapshot(snapshot) }
    }
}
