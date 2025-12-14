package com.example.recipeplanner.domain.usecase.recipe

import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

/**
 * Use case for searching recipes.
 */
class SearchRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(query: String): AppResult<List<Recipe>> {
        if (query.isBlank()) {
            return AppResult.Success(emptyList())
        }
        return recipeRepository.searchRecipes(query.trim())
    }
}
