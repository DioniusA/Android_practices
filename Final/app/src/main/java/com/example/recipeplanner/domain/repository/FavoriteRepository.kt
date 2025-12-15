package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.FavoriteRecipe
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(): Flow<List<FavoriteRecipe>>
    fun isFavorite(recipeId: String): Flow<Boolean>
    suspend fun addToFavorites(recipe: Recipe): AppResult<Unit>
    suspend fun removeFromFavorites(recipeId: String): AppResult<Unit>
    suspend fun toggleFavorite(recipe: Recipe): AppResult<Boolean>
    suspend fun syncFavorites(): AppResult<Unit>
}
