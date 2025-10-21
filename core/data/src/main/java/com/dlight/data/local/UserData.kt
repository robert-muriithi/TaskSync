package com.dlight.data.local


data class UserData(
    val userId: String?,
    val email: String?,
    val token: String?,
    val lastSyncTime: Long
)