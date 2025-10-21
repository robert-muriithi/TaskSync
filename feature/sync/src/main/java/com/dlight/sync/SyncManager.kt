package com.dlight.sync

import com.dlight.common.network.NetworkMonitor
import com.dlight.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages synchronization between local and remote tasks
 * Automatically syncs when network becomes available
 * 
 * Features:
 * - Auto-sync on network connection (immediate)
 * - State tracking for UI (syncState, lastSyncTime)
 * - Prevents duplicate concurrent syncs
 * - Exposes pending task count
 */
@Singleton
class SyncManager @Inject constructor(
    private val taskRepository: TaskRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    private val _pendingTaskCount = MutableStateFlow(0)
    val pendingTaskCount: StateFlow<Int> = _pendingTaskCount.asStateFlow()

    private var isInitialized = false
    private var syncScheduleJob: Job? = null

    /**
     * Start automatic syncing
     * Call this once at app startup
     * 
     * Features:
     * 1. Syncs immediately if network is available
     * 2. Monitors network changes and syncs when connection is restored
     * 3. Monitors pending tasks and syncs immediately when tasks are created/updated (if online)
     * 4. Updates pending task count
     * 5. Tracks sync state for UI
     */
    fun startAutoSync(scope: CoroutineScope) {
        if (isInitialized) {
            return
        }
        isInitialized = true

        scope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline) {
                        // Small delay to ensure network is fully established
                        delay(500)
                        syncTasks()
                    } else {
                        _syncState.value = SyncState.Idle
                    }
                }
        }

        scope.launch {
            taskRepository.observePendingTaskCount()
                .distinctUntilChanged()
                .collect { pendingCount ->
                    _pendingTaskCount.value = pendingCount
                    
                    if (syncScheduleJob?.isActive == true) {
                        syncScheduleJob?.cancel()
                    }

                    if (pendingCount > 0 && networkMonitor.isCurrentlyOnline()) {
                        syncScheduleJob = scope.launch {
                            delay(60_000)
                            syncScheduleJob = null
                            
                            if (_pendingTaskCount.value > 0 && networkMonitor.isCurrentlyOnline()) {
                                syncTasks()
                            } else {
                                Timber.d("Sync skipped - either no pending tasks or offline")
                            }
                        }
                    } else {
                        syncScheduleJob = null
                    }
                }
        }
        
        scope.launch {
            syncState.collect { state ->
                when (state) {
                    is SyncState.Idle -> Timber.d("Sync state: IDLE")
                    is SyncState.Syncing -> Timber.d("Sync state: SYNCING...")
                    is SyncState.Success -> Timber.d("Sync state: SUCCESS")
                    is SyncState.Error -> Timber.e("Sync state: ERROR - ${state.message}")
                }
            }
        }
    }

    suspend fun syncTasks() {
        if (_syncState.value is SyncState.Syncing) {
            return
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            _syncState.value = SyncState.Error("No network connection")
            return
        }

        try {
            _syncState.value = SyncState.Syncing

            val result = taskRepository.syncTasks()

            if (result) {
                _lastSyncTime.value = System.currentTimeMillis()
                _syncState.value = SyncState.Success
            } else {
                _syncState.value = SyncState.Error("Sync failed")
            }
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Unknown error")
        } finally {

        }
    }

    suspend fun getPendingSyncCount(): Int {
        return try {
            taskRepository.getPendingTasks().size
        } catch (_: Exception) {
            0
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}
