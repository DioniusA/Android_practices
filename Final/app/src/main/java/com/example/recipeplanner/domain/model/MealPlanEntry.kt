package com.example.recipeplanner.domain.model

import java.time.LocalDate

data class MealPlanEntry(
    val id: String,
    val userId: String,
    val recipeId: String,
    val recipeName: String,
    val recipeImageUrl: String,
    val date: LocalDate,
    val mealType: MealType
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}
