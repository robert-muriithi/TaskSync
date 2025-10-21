package com.dlight.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlight.domain.model.Task
import com.dlight.domain.usecase.auth.LogoutUseCase
import com.dlight.domain.usecase.task.GetTasksUseCase
import com.dlight.domain.usecase.task.SyncTasksUseCase
import com.dlight.domain.usecase.task.ToggleTaskCompleteUseCase
import com.dlight.sync.SyncManager
import com.dlight.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class TasksViewModel @Inject constructor(
    getTasksUseCase: GetTasksUseCase,
    private val toggleTaskCompleteUseCase: ToggleTaskCompleteUseCase,
    private val syncTasksUseCase: SyncTasksUseCase,
    private val logoutUseCase: LogoutUseCase,
    syncManager: SyncManager
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = getTasksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val syncState: StateFlow<String> = syncManager.syncState
        .map { state ->
            when (state) {
                is SyncState.Idle -> "IDLE"
                is SyncState.Syncing -> "SYNCING"
                is SyncState.Success -> "SUCCESS"
                is SyncState.Error -> "ERROR"
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "IDLE"
        )

    val pendingTaskCount: StateFlow<Int> = syncManager.pendingTaskCount
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                syncTasksUseCase()

                // small delay to show the refresh indicator
                delay(5000)
            } catch (e: Exception) {
                Timber.e("Refresh failed ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            try {
                toggleTaskCompleteUseCase(task)
            } catch (_: Exception) {
            }
        }
    }

    fun manualSync() {
        viewModelScope.launch {
            try {
                Timber.d("🔄 Manual sync triggered")
                syncTasksUseCase()
            } catch (e: Exception) {
                Timber.e(e, "❌ Manual sync failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                Timber.d("🔐 Logging out user...")
                logoutUseCase()
                Timber.d("✅ User logged out successfully")
            } catch (e: Exception) {
                Timber.e(e, "❌ Logout failed")
            }
        }
    }
}
