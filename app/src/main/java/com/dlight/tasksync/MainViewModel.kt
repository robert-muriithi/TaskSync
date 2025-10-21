package com.dlight.tasksync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlight.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        observeAuthState()
    }
    private fun observeAuthState() {
        viewModelScope.launch {
            userPreferences.observeIsLoggedIn().collect { loggedIn ->
                _isLoggedIn.value = loggedIn
                _isLoading.value = false
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUser()
        }
    }
}