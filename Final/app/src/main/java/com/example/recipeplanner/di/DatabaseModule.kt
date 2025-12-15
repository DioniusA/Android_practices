package com.example.recipeplanner.di

import android.content.Context
import androidx.room.Room
import com.example.recipeplanner.data.local.RecipePlannerDatabase
import com.example.recipeplanner.data.local.dao.FavoriteDao
import com.example.recipeplanner.data.local.dao.MealPlanDao
import com.example.recipeplanner.data.local.dao.RecipeDao
import com.example.recipeplanner.data.local.dao.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipePlannerDatabase {
        return Room.databaseBuilder(
            context,
            RecipePlannerDatabase::class.java,
            RecipePlannerDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(database: RecipePlannerDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: RecipePlannerDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideMealPlanDao(database: RecipePlannerDatabase): MealPlanDao {
        return database.mealPlanDao()
    }

    @Provides
    @Singleton
    fun provideShoppingListDao(database: RecipePlannerDatabase): ShoppingListDao {
        return database.shoppingListDao()
    }
}
