package com.example.recipeplanner.domain.usecase.recipe

import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

class GetRecipesByCategoryUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(category: String): AppResult<List<Recipe>> {
        return recipeRepository.getRecipesByCategory(category)
    }
}
