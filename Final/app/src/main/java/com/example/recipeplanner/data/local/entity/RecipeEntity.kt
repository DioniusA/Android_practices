package com.example.recipeplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching recipes locally.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnailUrl: String,
    val youtubeUrl: String?,
    val ingredientsJson: String, // JSON serialized list of ingredients
    val tagsJson: String, // JSON serialized list of tags
    val sourceUrl: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
