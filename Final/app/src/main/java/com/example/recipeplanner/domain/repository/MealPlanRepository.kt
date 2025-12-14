package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for meal plan operations.
 * Syncs between Supabase (remote) and Room (local cache).
 */
interface MealPlanRepository {
    /**
     * Flow of all meal plan entries for the current user.
     */
    fun getMealPlan(): Flow<List<MealPlanEntry>>

    /**
     * Gets meal plan entries for a specific week.
     */
    fun getMealPlanForWeek(startDate: LocalDate): Flow<List<MealPlanEntry>>

    /**
     * Gets meal plan entries for a specific date.
     */
    fun getMealPlanForDate(date: LocalDate): Flow<List<MealPlanEntry>>

    /**
     * Adds a recipe to the meal plan.
     */
    suspend fun addToMealPlan(
        recipe: Recipe,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit>

    /**
     * Removes an entry from the meal plan.
     */
    suspend fun removeFromMealPlan(entryId: String): AppResult<Unit>

    /**
     * Updates an existing meal plan entry.
     */
    suspend fun updateMealPlanEntry(
        entryId: String,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit>

    /**
     * Syncs meal plan from remote to local cache.
     */
    suspend fun syncMealPlan(): AppResult<Unit>
}
