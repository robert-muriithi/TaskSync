package com.dlight.domain.repository

import com.dlight.domain.model.User

interface AuthRepository {
    suspend fun login(email: String): User?
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
    suspend fun logout()
}
