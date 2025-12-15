package com.example.recipeplanner.domain.usecase.favorite

import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.FavoriteRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(recipe: Recipe): AppResult<Boolean> {
        return favoriteRepository.toggleFavorite(recipe)
    }
}
