package com.example.recipeplanner.di

import com.example.recipeplanner.data.repository.AuthRepositoryImpl
import com.example.recipeplanner.data.repository.FavoriteRepositoryImpl
import com.example.recipeplanner.data.repository.MealPlanRepositoryImpl
import com.example.recipeplanner.data.repository.RecipeRepositoryImpl
import com.example.recipeplanner.data.repository.SettingsRepositoryImpl
import com.example.recipeplanner.data.repository.ShoppingListRepositoryImpl
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.repository.FavoriteRepository
import com.example.recipeplanner.domain.repository.MealPlanRepository
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.repository.SettingsRepository
import com.example.recipeplanner.domain.repository.ShoppingListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository implementations to interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(impl: MealPlanRepositoryImpl): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindShoppingListRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
