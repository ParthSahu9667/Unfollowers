package com.unfollowlens.ui.dashboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfollowlens.data.db.entities.Snapshot
import com.unfollowlens.data.repository.SnapshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val hasData: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val mutualCount: Int = 0,
    val notFollowingBackCount: Int = 0,
    val fansCount: Int = 0,
    val lastSyncedAt: Long? = null,
    val deltaFollowers: Int? = null,
    val deltaFollowing: Int? = null,
    val isImporting: Boolean = false,
    val importError: String? = null,
    val importSuccess: Boolean = false,
    val currentSnapshotId: Long? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SnapshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val latest = repository.getLatestSnapshot()
            if (latest == null) {
                _uiState.update { it.copy(isLoading = false, hasData = false) }
                return@launch
            }
            val mutuals = repository.getMutuals(latest.id)
            val notBack = repository.getNotFollowingBack(latest.id)
            val fans = repository.getFans(latest.id)

            // Compute deltas
            val snapshots = repository.getLatestTwoSnapshots()
            var deltaF: Int? = null; var deltaFo: Int? = null
            if (snapshots.size == 2) {
                deltaF = latest.followerCount - snapshots[1].followerCount
                deltaFo = latest.followingCount - snapshots[1].followingCount
            }

            _uiState.update {
                it.copy(
                    isLoading = false, hasData = true,
                    followerCount = latest.followerCount, followingCount = latest.followingCount,
                    mutualCount = mutuals.size, notFollowingBackCount = notBack.size,
                    fansCount = fans.size, lastSyncedAt = latest.importedAt,
                    deltaFollowers = deltaF, deltaFollowing = deltaFo,
                    currentSnapshotId = latest.id, importSuccess = false
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
                    repository.pruneOldSnapshots()
                    _uiState.update { it.copy(isImporting = false, importSuccess = true) }
                    loadDashboard()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isImporting = false, importError = e.message ?: "Import failed") }
                }
            )
        }
    }

    fun dismissError() { _uiState.update { it.copy(importError = null) } }
}
