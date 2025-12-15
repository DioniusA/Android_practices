package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealPlanRepository {
    fun getMealPlan(): Flow<List<MealPlanEntry>>
    fun getMealPlanForWeek(startDate: LocalDate): Flow<List<MealPlanEntry>>
    fun getMealPlanForDate(date: LocalDate): Flow<List<MealPlanEntry>>
    
    suspend fun addToMealPlan(
        recipe: Recipe,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit>
    
    suspend fun removeFromMealPlan(entryId: String): AppResult<Unit>
    
    suspend fun updateMealPlanEntry(
        entryId: String,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit>
    
    suspend fun syncMealPlan(): AppResult<Unit>
}
