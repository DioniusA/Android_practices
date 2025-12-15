package com.example.recipeplanner.domain.model

import java.time.Instant

data class FavoriteRecipe(
    val id: String,
    val recipeId: String,
    val userId: String,
    val recipeName: String,
    val recipeImageUrl: String,
    val recipeCategory: String,
    val addedAt: Instant
)
