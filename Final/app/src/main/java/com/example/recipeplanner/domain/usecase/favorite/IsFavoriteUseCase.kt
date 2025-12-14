package com.example.recipeplanner.domain.usecase.favorite

import com.example.recipeplanner.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking if a recipe is a favorite.
 */
class IsFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(recipeId: String): Flow<Boolean> {
        return favoriteRepository.isFavorite(recipeId)
    }
}
