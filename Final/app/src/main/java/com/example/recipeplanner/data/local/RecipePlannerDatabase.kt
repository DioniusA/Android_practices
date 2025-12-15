package com.example.recipeplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.recipeplanner.data.local.dao.FavoriteDao
import com.example.recipeplanner.data.local.dao.MealPlanDao
import com.example.recipeplanner.data.local.dao.RecipeDao
import com.example.recipeplanner.data.local.dao.ShoppingListDao
import com.example.recipeplanner.data.local.entity.FavoriteEntity
import com.example.recipeplanner.data.local.entity.MealPlanEntity
import com.example.recipeplanner.data.local.entity.RecipeEntity
import com.example.recipeplanner.data.local.entity.ShoppingListEntity

@Database(
    entities = [
        RecipeEntity::class,
        FavoriteEntity::class,
        MealPlanEntity::class,
        ShoppingListEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RecipePlannerDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        const val DATABASE_NAME = "recipe_planner_db"
    }
}
