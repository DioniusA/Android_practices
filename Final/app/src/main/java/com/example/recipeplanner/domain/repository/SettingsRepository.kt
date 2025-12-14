package com.example.recipeplanner.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings.
 */
interface SettingsRepository {
    /**
     * Flow of the current theme mode.
     */
    val themeMode: Flow<ThemeMode>

    /**
     * Sets the theme mode.
     */
    suspend fun setThemeMode(mode: ThemeMode)
}

/**
 * Available theme modes.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
