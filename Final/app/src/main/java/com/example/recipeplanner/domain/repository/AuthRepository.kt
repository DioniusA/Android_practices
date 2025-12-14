package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    /**
     * Flow of the current authenticated user. Emits null when not authenticated.
     */
    val currentUser: Flow<User?>

    /**
     * Checks if a user is currently authenticated.
     */
    val isAuthenticated: Flow<Boolean>

    /**
     * Signs in a user with email and password.
     */
    suspend fun signIn(email: String, password: String): AppResult<User>

    /**
     * Registers a new user with email and password.
     */
    suspend fun signUp(email: String, password: String): AppResult<User>

    /**
     * Signs out the current user.
     */
    suspend fun signOut(): AppResult<Unit>

    /**
     * Gets the current user ID or null if not authenticated.
     */
    suspend fun getCurrentUserId(): String?
}
