package com.example.recipeplanner.domain.usecase.favorite

import com.example.recipeplanner.domain.model.FavoriteRecipe
import com.example.recipeplanner.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all favorite recipes.
 */
class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<FavoriteRecipe>> {
        return favoriteRepository.getFavorites()
    }
}
