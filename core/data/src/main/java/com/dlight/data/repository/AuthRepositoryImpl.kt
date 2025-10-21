package com.dlight.data.repository

import com.dlight.data.local.UserPreferences
import com.dlight.domain.model.User
import com.dlight.domain.repository.AuthRepository
import com.dlight.network.api.AuthApiService
import com.dlight.network.model.LoginRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override suspend fun login(email: String): User? {
        return try {
            val response = authApi.login(LoginRequest(email))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                userPreferences.saveUser(
                    userId = loginResponse.id,
                    email = loginResponse.email,
                    token = loginResponse.token
                )
                User(
                    id = loginResponse.id,
                    email = loginResponse.email,
                    token = loginResponse.token
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            null
        }
    }

    override suspend fun getCurrentUser(): User? {
        val userId = userPreferences.getUserId()
        val email = userPreferences.getEmail()
        val token = userPreferences.getToken()
        
        return if (userId != null && email != null && token != null) {
            User(id = userId, email = email, token = token)
        } else {
            null
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return userPreferences.isLoggedIn()
    }

    override suspend fun logout() {
        userPreferences.clearUser()
        Timber.d("User logged out")
    }
}
