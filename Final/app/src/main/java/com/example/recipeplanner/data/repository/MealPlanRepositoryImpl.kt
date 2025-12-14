package com.example.recipeplanner.data.repository

import com.example.recipeplanner.data.local.dao.MealPlanDao
import com.example.recipeplanner.data.mapper.toDto
import com.example.recipeplanner.data.mapper.toEntity
import com.example.recipeplanner.data.mapper.toMealPlanEntry
import com.example.recipeplanner.data.remote.dto.MealPlanEntryDto
import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.repository.MealPlanRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MealPlanRepository with Supabase and Room offline support.
 */
@Singleton
class MealPlanRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val mealPlanDao: MealPlanDao,
    private val authRepository: AuthRepository
) : MealPlanRepository {

    private val table = "meal_plan_entries"

    override fun getMealPlan(): Flow<List<MealPlanEntry>> {
        return mealPlanDao.getAllMealPlan().map { entities ->
            entities.map { it.toMealPlanEntry() }
        }
    }

    override fun getMealPlanForWeek(startDate: LocalDate): Flow<List<MealPlanEntry>> {
        val endDate = startDate.plusDays(6)
        return mealPlanDao.getAllMealPlan().map { entities ->
            entities.map { it.toMealPlanEntry() }
                .filter { it.date >= startDate && it.date <= endDate }
        }
    }

    override fun getMealPlanForDate(date: LocalDate): Flow<List<MealPlanEntry>> {
        return mealPlanDao.getAllMealPlan().map { entities ->
            entities.map { it.toMealPlanEntry() }
                .filter { it.date == date }
        }
    }

    override suspend fun addToMealPlan(
        recipe: Recipe,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        val entry = MealPlanEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            recipeId = recipe.id,
            recipeName = recipe.name,
            recipeImageUrl = recipe.thumbnailUrl,
            date = date,
            mealType = mealType
        )

        // Save locally first
        mealPlanDao.insert(entry.toEntity())

        // Sync to Supabase
        return try {
            supabaseClient.postgrest[table].insert(entry.toDto())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync meal plan to Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun removeFromMealPlan(entryId: String): AppResult<Unit> {
        // Remove locally first
        mealPlanDao.delete(entryId)

        // Sync to Supabase
        return try {
            supabaseClient.postgrest[table].delete {
                filter {
                    eq("id", entryId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove meal plan entry from Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun updateMealPlanEntry(
        entryId: String,
        date: LocalDate,
        mealType: MealType
    ): AppResult<Unit> {
        val existing = mealPlanDao.getById(entryId)
            ?: return AppResult.Error(AppError.NotFound("Entry not found"))

        val updated = existing.copy(
            date = date.toEpochDay(),
            mealType = mealType.name
        )

        // Update locally first
        mealPlanDao.insert(updated)

        // Sync to Supabase
        return try {
            supabaseClient.postgrest[table].update({
                set("date", date.toString())
                set("meal_type", mealType.name)
            }) {
                filter {
                    eq("id", entryId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update meal plan entry in Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun syncMealPlan(): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        return try {
            val response = supabaseClient.postgrest[table]
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<MealPlanEntryDto>()

            // Update local cache
            mealPlanDao.deleteAllByUser(userId)
            mealPlanDao.insertAll(response.map { it.toMealPlanEntry().toEntity() })

            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync meal plan")
            AppResult.Error(AppError.NetworkError(cause = e))
        }
    }
}
