package com.example.recipeplanner.presentation

import androidx.lifecycle.ViewModel
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.repository.SettingsRepository
import com.example.recipeplanner.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val themeMode: Flow<ThemeMode> = settingsRepository.themeMode
    val isAuthenticated: Flow<Boolean> = authRepository.isAuthenticated
}
