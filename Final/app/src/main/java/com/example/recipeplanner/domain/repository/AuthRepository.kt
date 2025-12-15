package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isAuthenticated: Flow<Boolean>
    
    suspend fun signIn(email: String, password: String): AppResult<User>
    suspend fun signUp(email: String, password: String): AppResult<User>
    suspend fun signOut(): AppResult<Unit>
    suspend fun getCurrentUserId(): String?
}
