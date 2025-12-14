package com.example.recipeplanner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for user profile in Supabase.
 */
@Serializable
data class ProfileDto(
    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * DTO for favorite recipe in Supabase.
 */
@Serializable
data class FavoriteDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("recipe_id")
    val recipeId: String,

    @SerialName("recipe_name")
    val recipeName: String,

    @SerialName("recipe_image_url")
    val recipeImageUrl: String,

    @SerialName("recipe_category")
    val recipeCategory: String,

    @SerialName("added_at")
    val addedAt: String
)

/**
 * DTO for meal plan entry in Supabase.
 */
@Serializable
data class MealPlanEntryDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("recipe_id")
    val recipeId: String,

    @SerialName("recipe_name")
    val recipeName: String,

    @SerialName("recipe_image_url")
    val recipeImageUrl: String,

    @SerialName("date")
    val date: String, // ISO date string

    @SerialName("meal_type")
    val mealType: String
)

/**
 * DTO for shopping list item in Supabase.
 */
@Serializable
data class ShoppingListItemDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("ingredient_name")
    val ingredientName: String,

    @SerialName("quantity")
    val quantity: String,

    @SerialName("category")
    val category: String,

    @SerialName("is_checked")
    val isChecked: Boolean,

    @SerialName("recipe_ids")
    val recipeIds: List<String>
)
