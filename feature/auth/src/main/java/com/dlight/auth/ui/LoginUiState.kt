package com.dlight.auth.ui

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null
)
