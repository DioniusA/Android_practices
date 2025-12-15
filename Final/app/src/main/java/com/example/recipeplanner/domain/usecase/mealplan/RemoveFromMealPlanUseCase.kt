package com.example.recipeplanner.domain.usecase.mealplan

import com.example.recipeplanner.domain.repository.MealPlanRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

class RemoveFromMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(entryId: String): AppResult<Unit> {
        return mealPlanRepository.removeFromMealPlan(entryId)
    }
}
