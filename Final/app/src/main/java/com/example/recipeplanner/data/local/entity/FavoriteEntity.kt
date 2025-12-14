package com.example.recipeplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching favorites locally.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val userId: String,
    val recipeName: String,
    val recipeImageUrl: String,
    val recipeCategory: String,
    val addedAt: Long
)
