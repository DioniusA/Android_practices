package com.example.recipeplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plan")
data class MealPlanEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val recipeId: String,
    val recipeName: String,
    val recipeImageUrl: String,
    val date: Long, // Epoch day
    val mealType: String // BREAKFAST, LUNCH, DINNER
)
