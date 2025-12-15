package com.example.recipeplanner.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
