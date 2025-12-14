package com.example.recipeplanner.data.mapper

import com.example.recipeplanner.data.local.entity.FavoriteEntity
import com.example.recipeplanner.data.remote.dto.FavoriteDto
import com.example.recipeplanner.domain.model.FavoriteRecipe
import java.time.Instant

/**
 * Maps FavoriteDto from Supabase to domain FavoriteRecipe model.
 */
fun FavoriteDto.toFavoriteRecipe(): FavoriteRecipe {
    return FavoriteRecipe(
        id = id,
        recipeId = recipeId,
        userId = userId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        recipeCategory = recipeCategory,
        addedAt = try {
            Instant.parse(addedAt)
        } catch (e: Exception) {
            Instant.now()
        }
    )
}

/**
 * Maps FavoriteEntity from local storage to domain FavoriteRecipe model.
 */
fun FavoriteEntity.toFavoriteRecipe(): FavoriteRecipe {
    return FavoriteRecipe(
        id = id,
        recipeId = recipeId,
        userId = userId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        recipeCategory = recipeCategory,
        addedAt = Instant.ofEpochMilli(addedAt)
    )
}

/**
 * Maps domain FavoriteRecipe to FavoriteEntity for local storage.
 */
fun FavoriteRecipe.toEntity(): FavoriteEntity {
    return FavoriteEntity(
        id = id,
        recipeId = recipeId,
        userId = userId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        recipeCategory = recipeCategory,
        addedAt = addedAt.toEpochMilli()
    )
}

/**
 * Maps domain FavoriteRecipe to FavoriteDto for Supabase.
 */
fun FavoriteRecipe.toDto(): FavoriteDto {
    return FavoriteDto(
        id = id,
        userId = userId,
        recipeId = recipeId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        recipeCategory = recipeCategory,
        addedAt = addedAt.toString()
    )
}
