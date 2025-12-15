package com.example.recipeplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipeplanner.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plan WHERE userId = :userId ORDER BY date, mealType")
    fun getMealPlanByUser(userId: String): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan ORDER BY date, mealType")
    fun getAllMealPlan(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date, mealType")
    fun getMealPlanForWeek(userId: String, startDate: Long, endDate: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan WHERE userId = :userId AND date = :date ORDER BY mealType")
    fun getMealPlanForDate(userId: String, date: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan WHERE id = :id")
    suspend fun getById(id: String): MealPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MealPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MealPlanEntity>)

    @Query("DELETE FROM meal_plan WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM meal_plan WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM meal_plan")
    suspend fun clearAll()
}
