package com.example.recipeplanner.domain.usecase.mealplan

import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.MealPlanRepository
import com.example.recipeplanner.domain.util.AppResult
import java.time.LocalDate
import javax.inject.Inject

class AddToMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(
        recipe: Recipe,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit> {
        return mealPlanRepository.addToMealPlan(recipe, date, mealType)
    }
}
