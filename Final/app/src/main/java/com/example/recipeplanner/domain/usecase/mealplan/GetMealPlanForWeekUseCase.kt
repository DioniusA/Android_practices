package com.example.recipeplanner.domain.usecase.mealplan

import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for getting the meal plan for a week.
 */
class GetMealPlanForWeekUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    operator fun invoke(startDate: LocalDate): Flow<List<MealPlanEntry>> {
        return mealPlanRepository.getMealPlanForWeek(startDate)
    }
}
