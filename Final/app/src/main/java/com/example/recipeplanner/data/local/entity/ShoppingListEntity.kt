package com.example.recipeplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching shopping list items locally.
 */
@Entity(tableName = "shopping_list")
data class ShoppingListEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val ingredientName: String,
    val quantity: String,
    val category: String,
    val isChecked: Boolean,
    val recipeIdsJson: String // JSON serialized list
)
