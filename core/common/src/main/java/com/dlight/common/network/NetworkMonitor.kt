package com.dlight.common.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    suspend fun isCurrentlyOnline(): Boolean
}
