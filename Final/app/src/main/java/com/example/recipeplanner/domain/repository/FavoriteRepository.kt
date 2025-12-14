package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.FavoriteRecipe
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for favorite recipes operations.
 * Syncs between Supabase (remote) and Room (local cache).
 */
interface FavoriteRepository {
    /**
     * Flow of all favorite recipes for the current user.
     * Emits from local cache first, then syncs with remote.
     */
    fun getFavorites(): Flow<List<FavoriteRecipe>>

    /**
     * Checks if a recipe is in favorites.
     */
    fun isFavorite(recipeId: String): Flow<Boolean>

    /**
     * Adds a recipe to favorites.
     * Saves to both local cache and remote.
     */
    suspend fun addToFavorites(recipe: Recipe): AppResult<Unit>

    /**
     * Removes a recipe from favorites.
     * Removes from both local cache and remote.
     */
    suspend fun removeFromFavorites(recipeId: String): AppResult<Unit>

    /**
     * Toggles the favorite status of a recipe.
     */
    suspend fun toggleFavorite(recipe: Recipe): AppResult<Boolean>

    /**
     * Syncs favorites from remote to local cache.
     */
    suspend fun syncFavorites(): AppResult<Unit>
}
