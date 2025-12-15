package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun searchRecipes(query: String): AppResult<List<Recipe>>
    suspend fun getRecipesByCategory(category: String): AppResult<List<Recipe>>
    suspend fun getRecipeById(id: String): AppResult<Recipe>
    suspend fun getCategories(): AppResult<List<Category>>
    suspend fun getRandomRecipe(): AppResult<Recipe>
    fun getCachedRecipes(): Flow<List<Recipe>>
    suspend fun clearCache()
}
