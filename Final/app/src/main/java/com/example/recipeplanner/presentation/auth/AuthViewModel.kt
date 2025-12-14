package com.example.recipeplanner.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.usecase.auth.GetCurrentUserUseCase
import com.example.recipeplanner.domain.usecase.auth.SignInUseCase
import com.example.recipeplanner.domain.usecase.auth.SignUpUseCase
import com.example.recipeplanner.domain.util.AppResult
import com.example.recipeplanner.presentation.common.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isLoginMode: Boolean = true,
    val error: String? = null
)

sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : AuthEvent()
    data object ToggleMode : AuthEvent()
    data object Submit : AuthEvent()
    data object ClearError : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    val currentUser = getCurrentUserUseCase()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, error = null) }
            }
            is AuthEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, error = null) }
            }
            is AuthEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.confirmPassword, error = null) }
            }
            AuthEvent.ToggleMode -> {
                _uiState.update { 
                    it.copy(
                        isLoginMode = !it.isLoginMode, 
                        error = null,
                        confirmPassword = ""
                    ) 
                }
            }
            AuthEvent.Submit -> submit()
            AuthEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result: AppResult<User> = if (state.isLoginMode) {
                signInUseCase(state.email, state.password)
            } else {
                signUpUseCase(state.email, state.password, state.confirmPassword)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(UiEffect.ShowSnackbar("Welcome, ${result.data.email}!"))
                }
                is AppResult.Error -> {
                    _uiState.update { 
                        it.copy(isLoading = false, error = result.error.message) 
                    }
                }
            }
        }
    }
}
