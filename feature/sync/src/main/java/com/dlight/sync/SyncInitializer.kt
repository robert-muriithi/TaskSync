package com.dlight.sync

import com.dlight.sync.worker.SyncWorkScheduler
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes sync components at app startup
 * Call this from your Application class
 * 
 * Responsibilities:
 * 1. Start automatic network-based sync (SyncManager)
 * 2. Schedule periodic background sync (WorkManager)
 */
@Singleton
class SyncInitializer @Inject constructor(
    private val syncManager: SyncManager,
    private val syncWorkScheduler: SyncWorkScheduler
) {

    /**
     * Initialize all sync components
     * 
     * What happens:
     * 1. SyncManager starts monitoring network and syncs immediately if online
     * 2. WorkManager schedules periodic sync every 1 minutes
     * 3. Sync state flows are collected and logged
     * 
     * @param applicationScope CoroutineScope tied to Application lifecycle
     */
    fun initialize(applicationScope: CoroutineScope) {
        syncManager.startAutoSync(applicationScope)
        syncWorkScheduler.schedulePeriodicSync()
    }

    fun shutdown() {
        syncWorkScheduler.cancelSync()
    }
}
