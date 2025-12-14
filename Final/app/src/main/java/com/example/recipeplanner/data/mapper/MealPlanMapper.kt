package com.example.recipeplanner.data.mapper

import com.example.recipeplanner.data.local.entity.MealPlanEntity
import com.example.recipeplanner.data.remote.dto.MealPlanEntryDto
import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import java.time.LocalDate

/**
 * Maps MealPlanEntryDto from Supabase to domain MealPlanEntry model.
 */
fun MealPlanEntryDto.toMealPlanEntry(): MealPlanEntry {
    return MealPlanEntry(
        id = id,
        userId = userId,
        recipeId = recipeId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        date = LocalDate.parse(date),
        mealType = try {
            MealType.valueOf(mealType.uppercase())
        } catch (e: Exception) {
            MealType.DINNER
        }
    )
}

/**
 * Maps MealPlanEntity from local storage to domain MealPlanEntry model.
 */
fun MealPlanEntity.toMealPlanEntry(): MealPlanEntry {
    return MealPlanEntry(
        id = id,
        userId = userId,
        recipeId = recipeId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        date = LocalDate.ofEpochDay(date),
        mealType = try {
            MealType.valueOf(mealType.uppercase())
        } catch (e: Exception) {
            MealType.DINNER
        }
    )
}

/**
 * Maps domain MealPlanEntry to MealPlanEntity for local storage.
 */
fun MealPlanEntry.toEntity(): MealPlanEntity {
    return MealPlanEntity(
        id = id,
        userId = userId,
        recipeId = recipeId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        date = date.toEpochDay(),
        mealType = mealType.name
    )
}

/**
 * Maps domain MealPlanEntry to MealPlanEntryDto for Supabase.
 */
fun MealPlanEntry.toDto(): MealPlanEntryDto {
    return MealPlanEntryDto(
        id = id,
        userId = userId,
        recipeId = recipeId,
        recipeName = recipeName,
        recipeImageUrl = recipeImageUrl,
        date = date.toString(),
        mealType = mealType.name
    )
}
