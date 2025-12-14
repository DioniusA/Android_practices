package com.example.recipeplanner.domain.model

import java.time.Instant

/**
 * Represents a recipe that has been marked as favorite by the user.
 * Contains a snapshot of recipe data for offline access.
 */
data class FavoriteRecipe(
    val id: String,
    val recipeId: String,
    val userId: String,
    val recipeName: String,
    val recipeImageUrl: String,
    val recipeCategory: String,
    val addedAt: Instant
)
