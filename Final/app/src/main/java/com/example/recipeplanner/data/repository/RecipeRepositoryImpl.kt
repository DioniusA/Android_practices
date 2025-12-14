package com.example.recipeplanner.data.repository

import com.example.recipeplanner.data.local.dao.RecipeDao
import com.example.recipeplanner.data.mapper.toCategory
import com.example.recipeplanner.data.mapper.toEntity
import com.example.recipeplanner.data.mapper.toRecipe
import com.example.recipeplanner.data.remote.api.MealDbApi
import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import com.example.recipeplanner.domain.util.toAppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecipeRepository using TheMealDB API with Room caching.
 */
@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val api: MealDbApi,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override suspend fun searchRecipes(query: String): AppResult<List<Recipe>> {
        return try {
            val response = api.searchMealsByName(query)
            val recipes = response.meals?.map { it.toRecipe() } ?: emptyList()

            // Cache the recipes
            if (recipes.isNotEmpty()) {
                recipeDao.insertRecipes(recipes.map { it.toEntity() })
            }

            AppResult.Success(recipes)
        } catch (e: Exception) {
            Timber.e(e, "Failed to search recipes")
            // Try to return cached results
            val cached = recipeDao.searchRecipes(query).map { it.toRecipe() }
            if (cached.isNotEmpty()) {
                AppResult.Success(cached)
            } else {
                AppResult.Error(e.toAppError())
            }
        }
    }

    override suspend fun getRecipesByCategory(category: String): AppResult<List<Recipe>> {
        return try {
            // First get the list of meals in the category (only contains id, name, thumb)
            val filterResponse = api.filterByCategory(category)
            val mealIds = filterResponse.meals?.map { it.idMeal } ?: emptyList()

            // For each meal, get the full details
            val recipes = mealIds.take(20).mapNotNull { id ->
                try {
                    val response = api.getMealById(id)
                    response.meals?.firstOrNull()?.toRecipe()
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get recipe $id")
                    null
                }
            }

            // Cache the recipes
            if (recipes.isNotEmpty()) {
                recipeDao.insertRecipes(recipes.map { it.toEntity() })
            }

            AppResult.Success(recipes)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get recipes by category")
            // Try to return cached results
            val cached = recipeDao.getRecipesByCategory(category).map { it.toRecipe() }
            if (cached.isNotEmpty()) {
                AppResult.Success(cached)
            } else {
                AppResult.Error(e.toAppError())
            }
        }
    }

    override suspend fun getRecipeById(id: String): AppResult<Recipe> {
        // First check cache
        val cached = recipeDao.getRecipeById(id)
        if (cached != null) {
            return AppResult.Success(cached.toRecipe())
        }

        // Fetch from API
        return try {
            val response = api.getMealById(id)
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                val recipe = meal.toRecipe()
                recipeDao.insertRecipe(recipe.toEntity())
                AppResult.Success(recipe)
            } else {
                AppResult.Error(AppError.NotFound("Recipe not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get recipe by id")
            AppResult.Error(e.toAppError())
        }
    }

    override suspend fun getCategories(): AppResult<List<Category>> {
        return try {
            val response = api.getCategories()
            val categories = response.categories?.map { it.toCategory() } ?: emptyList()
            AppResult.Success(categories)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get categories")
            AppResult.Error(e.toAppError())
        }
    }

    override suspend fun getRandomRecipe(): AppResult<Recipe> {
        return try {
            val response = api.getRandomMeal()
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                val recipe = meal.toRecipe()
                recipeDao.insertRecipe(recipe.toEntity())
                AppResult.Success(recipe)
            } else {
                AppResult.Error(AppError.NotFound("No random recipe found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get random recipe")
            AppResult.Error(e.toAppError())
        }
    }

    override fun getCachedRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    override suspend fun clearCache() {
        recipeDao.clearAll()
    }
}
