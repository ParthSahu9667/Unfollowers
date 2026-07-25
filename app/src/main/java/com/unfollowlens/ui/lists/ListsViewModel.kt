package com.unfollowlens.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unfollowlens.data.db.entities.FollowerRecord
import com.unfollowlens.data.db.entities.ListType
import com.unfollowlens.data.repository.SnapshotRepository
import android.content.Context
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ListCategory { NOT_BACK, FANS, MUTUALS, ALL_FOLLOWERS, ALL_FOLLOWING }

data class ListsUiState(
    val isLoading: Boolean = true,
    val hasData: Boolean = false,
    val selectedCategory: ListCategory = ListCategory.NOT_BACK,
    val searchQuery: String = "",
    val users: List<FollowerRecord> = emptyList(),
    val filteredUsers: List<FollowerRecord> = emptyList(),
    val snapshotId: Long? = null
)

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val repository: SnapshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val snapshot = repository.getLatestSnapshot()
            if (snapshot == null) {
                _uiState.update { it.copy(isLoading = false, hasData = false) }
                return@launch
            }
            _uiState.update { it.copy(snapshotId = snapshot.id, hasData = true) }
            loadCategory(_uiState.value.selectedCategory)
        }
    }

    fun selectCategory(category: ListCategory) {
        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") }
        loadCategory(category)
    }

    fun setInitialCategory(category: String) {
        val cat = when (category) {
            "not_back" -> ListCategory.NOT_BACK
            "fans" -> ListCategory.FANS
            "mutuals" -> ListCategory.MUTUALS
            "all_followers" -> ListCategory.ALL_FOLLOWERS
            "all_following" -> ListCategory.ALL_FOLLOWING
            else -> ListCategory.NOT_BACK
        }
        selectCategory(cat)
    }

    fun updateSearch(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) state.users
            else state.users.filter { it.username.contains(query, ignoreCase = true) }
            state.copy(searchQuery = query, filteredUsers = filtered)
        }
    }

    private fun loadCategory(category: ListCategory) {
        val sid = _uiState.value.snapshotId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val usernames: Set<String> = when (category) {
                ListCategory.NOT_BACK -> repository.getNotFollowingBack(sid)
                ListCategory.FANS -> repository.getFans(sid)
                ListCategory.MUTUALS -> repository.getMutuals(sid)
                ListCategory.ALL_FOLLOWERS -> repository.getFans(sid) + repository.getMutuals(sid) // all followers
                ListCategory.ALL_FOLLOWING -> repository.getNotFollowingBack(sid) + repository.getMutuals(sid)
            }

            val listType = when (category) {
                ListCategory.FANS, ListCategory.ALL_FOLLOWERS -> ListType.FOLLOWER
                else -> ListType.FOLLOWING
            }

            // For ALL categories, get the records matching the usernames
            if (category == ListCategory.ALL_FOLLOWERS || category == ListCategory.ALL_FOLLOWING) {
                repository.getRecordsByUsernames(sid, listType, usernames.toList()).collect { records ->
                    _uiState.update { it.copy(isLoading = false, users = records, filteredUsers = records) }
                }
            } else {
                // For filtered categories, we need records from the appropriate type
                val recordType = when (category) {
                    ListCategory.NOT_BACK -> ListType.FOLLOWING
                    ListCategory.FANS -> ListType.FOLLOWER
                    ListCategory.MUTUALS -> ListType.FOLLOWER
                    else -> ListType.FOLLOWER
                }
                repository.getRecordsByUsernames(sid, recordType, usernames.toList()).collect { records ->
                    _uiState.update { it.copy(isLoading = false, users = records, filteredUsers = records) }
                }
            }
        }
    }

    fun writeCsvToUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.bufferedWriter().use { writer ->
                        writer.write("username,profile_url,category\n")
                        _uiState.value.filteredUsers.forEach { record ->
                            val url = record.profileUrl ?: ""
                            writer.write("${record.username},$url,${_uiState.value.selectedCategory.name}\n")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
