package com.example.recipeplanner.data.remote.api

import com.example.recipeplanner.data.remote.dto.CategoriesResponse
import com.example.recipeplanner.data.remote.dto.MealsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for TheMealDB.
 * Documentation: https://www.themealdb.com/api.php
 */
interface MealDbApi {
    /**
     * Search meals by name.
     */
    @GET("search.php")
    suspend fun searchMealsByName(@Query("s") name: String): MealsResponse

    /**
     * Lookup meal by ID.
     */
    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealsResponse

    /**
     * Get all meal categories.
     */
    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse

    /**
     * Filter by category.
     */
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealsResponse

    /**
     * Get a random meal.
     */
    @GET("random.php")
    suspend fun getRandomMeal(): MealsResponse

    /**
     * List all categories (simple list).
     */
    @GET("list.php?c=list")
    suspend fun listCategories(): MealsResponse

    /**
     * List all areas.
     */
    @GET("list.php?a=list")
    suspend fun listAreas(): MealsResponse
}
