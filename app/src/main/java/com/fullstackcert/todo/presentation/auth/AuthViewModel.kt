package com.fullstackcert.todo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fullstackcert.todo.domain.usecase.CheckSessionUseCase
import com.fullstackcert.todo.domain.usecase.GetSavedCredentialsUseCase
import com.fullstackcert.todo.domain.usecase.LoginUseCase
import com.fullstackcert.todo.domain.usecase.LogoutUseCase
import com.fullstackcert.todo.domain.usecase.RegisterUseCase
import com.fullstackcert.todo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isSessionChecked: Boolean = false,
    val registrationSuccess: Boolean = false,
    val savedUsername: String = "",
    val rememberMe: Boolean = false,
    val usernameError: String? = null,
    val isCheckingUsername: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkSessionUseCase: CheckSessionUseCase,
    private val getSavedCredentialsUseCase: GetSavedCredentialsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val (username, rememberMe) = getSavedCredentialsUseCase()
            _state.update { it.copy(savedUsername = username, rememberMe = rememberMe) }
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            val authenticated = checkSessionUseCase()
            _state.update { it.copy(isAuthenticated = authenticated, isSessionChecked = true) }
        }
    }

    fun login(username: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(username, password, rememberMe)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, isAuthenticated = true) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = registerUseCase(username, password)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, registrationSuccess = true) }
                is Resource.Error -> {
                    val usernameErr = if (result.message.contains("username", ignoreCase = true) ||
                        result.message.contains("taken", ignoreCase = true) ||
                        result.message.contains("already", ignoreCase = true)
                    ) "Name already exist" else null
                    _state.update { it.copy(isLoading = false, error = if (usernameErr == null) result.message else null, usernameError = usernameErr) }
                }
            }
        }
    }

    fun clearUsernameError() {
        _state.update { it.copy(usernameError = null) }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.update { AuthUiState() }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetRegistration() {
        _state.update { it.copy(registrationSuccess = false) }
    }
}
