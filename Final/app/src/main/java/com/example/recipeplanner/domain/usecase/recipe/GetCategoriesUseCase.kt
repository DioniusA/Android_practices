package com.example.recipeplanner.domain.usecase.recipe

import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(): AppResult<List<Category>> {
        return recipeRepository.getCategories()
    }
}
