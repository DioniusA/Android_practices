package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recipe operations from external API.
 */
interface RecipeRepository {
    /**
     * Searches for recipes by name.
     * Results are cached locally for offline access.
     */
    suspend fun searchRecipes(query: String): AppResult<List<Recipe>>

    /**
     * Gets recipes by category.
     */
    suspend fun getRecipesByCategory(category: String): AppResult<List<Recipe>>

    /**
     * Gets a recipe by its ID.
     * First checks local cache, then fetches from API if not found.
     */
    suspend fun getRecipeById(id: String): AppResult<Recipe>

    /**
     * Gets all available categories.
     */
    suspend fun getCategories(): AppResult<List<Category>>

    /**
     * Gets a random recipe.
     */
    suspend fun getRandomRecipe(): AppResult<Recipe>

    /**
     * Gets cached recipes as a Flow for offline-first experience.
     */
    fun getCachedRecipes(): Flow<List<Recipe>>

    /**
     * Clears the local recipe cache.
     */
    suspend fun clearCache()
}
