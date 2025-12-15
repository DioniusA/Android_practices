package com.example.recipeplanner.data.repository

import com.example.recipeplanner.data.local.dao.FavoriteDao
import com.example.recipeplanner.data.mapper.toDto
import com.example.recipeplanner.data.mapper.toEntity
import com.example.recipeplanner.data.mapper.toFavoriteRecipe
import com.example.recipeplanner.data.remote.dto.FavoriteDto
import com.example.recipeplanner.domain.model.FavoriteRecipe
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.repository.FavoriteRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val favoriteDao: FavoriteDao,
    private val authRepository: AuthRepository
) : FavoriteRepository {

    private val table = "favorites"

    override fun getFavorites(): Flow<List<FavoriteRecipe>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toFavoriteRecipe() }
        }
    }

    override fun isFavorite(recipeId: String): Flow<Boolean> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.any { it.recipeId == recipeId }
        }
    }

    override suspend fun addToFavorites(recipe: Recipe): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        val favorite = FavoriteRecipe(
            id = UUID.randomUUID().toString(),
            recipeId = recipe.id,
            userId = userId,
            recipeName = recipe.name,
            recipeImageUrl = recipe.thumbnailUrl,
            recipeCategory = recipe.category,
            addedAt = Instant.now()
        )

        favoriteDao.insert(favorite.toEntity())

        return try {
            supabaseClient.postgrest[table].insert(favorite.toDto())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync favorite to Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun removeFromFavorites(recipeId: String): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        favoriteDao.delete(recipeId, userId)

        return try {
            supabaseClient.postgrest[table].delete {
                filter {
                    eq("recipe_id", recipeId)
                    eq("user_id", userId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove favorite from Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun toggleFavorite(recipe: Recipe): AppResult<Boolean> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        val existing = favoriteDao.getFavorite(recipe.id, userId)
        return if (existing != null) {
            removeFromFavorites(recipe.id).map { false }
        } else {
            addToFavorites(recipe).map { true }
        }
    }

    override suspend fun syncFavorites(): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        return try {
            val response = supabaseClient.postgrest[table]
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FavoriteDto>()

            favoriteDao.deleteAllByUser(userId)
            favoriteDao.insertAll(response.map { it.toFavoriteRecipe().toEntity() })

            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync favorites")
            AppResult.Error(AppError.NetworkError(cause = e))
        }
    }
}
