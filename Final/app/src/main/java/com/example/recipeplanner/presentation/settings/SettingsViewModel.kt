package com.example.recipeplanner.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.repository.SettingsRepository
import com.example.recipeplanner.domain.repository.ThemeMode
import com.example.recipeplanner.domain.usecase.auth.SignOutUseCase
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

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showLogoutDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val isLoggingOut: Boolean = false
)

sealed class SettingsEvent {
    data class SetThemeMode(val mode: ThemeMode) : SettingsEvent()
    data object ShowLogoutDialog : SettingsEvent()
    data object DismissLogoutDialog : SettingsEvent()
    data object ConfirmLogout : SettingsEvent()
    data object ShowAboutDialog : SettingsEvent()
    data object DismissAboutDialog : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    init {
        observeThemeMode()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetThemeMode -> setThemeMode(event.mode)
            SettingsEvent.ShowLogoutDialog -> {
                _uiState.update { it.copy(showLogoutDialog = true) }
            }
            SettingsEvent.DismissLogoutDialog -> {
                _uiState.update { it.copy(showLogoutDialog = false) }
            }
            SettingsEvent.ConfirmLogout -> logout()
            SettingsEvent.ShowAboutDialog -> {
                _uiState.update { it.copy(showAboutDialog = true) }
            }
            SettingsEvent.DismissAboutDialog -> {
                _uiState.update { it.copy(showAboutDialog = false) }
            }
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    private fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, showLogoutDialog = false) }
            
            when (val result = signOutUseCase()) {
                is AppResult.Success -> {
                    _effects.emit(UiEffect.ShowSnackbar("Signed out successfully"))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
            
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }
}
