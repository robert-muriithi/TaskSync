package com.dlight.domain.usecase.auth

import com.dlight.domain.model.User
import com.dlight.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    private val emailValidator = EmailValidator()
    suspend operator fun invoke(email: String): User {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.contains("@")) { "Invalid email format" }
        
        return authRepository.login(email)
            ?: throw IllegalStateException("Login failed: No user returned")
    }
}
