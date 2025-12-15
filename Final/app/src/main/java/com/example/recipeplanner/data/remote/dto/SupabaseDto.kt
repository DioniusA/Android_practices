package com.example.recipeplanner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val date: String,

    @SerialName("meal_type")
    val mealType: String
)

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
