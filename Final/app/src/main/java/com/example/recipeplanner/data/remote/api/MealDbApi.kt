package com.example.recipeplanner.data.remote.api

import com.example.recipeplanner.data.remote.dto.CategoriesResponse
import com.example.recipeplanner.data.remote.dto.MealsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {
    @GET("search.php")
    suspend fun searchMealsByName(@Query("s") name: String): MealsResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealsResponse

    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealsResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealsResponse

    @GET("list.php?c=list")
    suspend fun listCategories(): MealsResponse

    @GET("list.php?a=list")
    suspend fun listAreas(): MealsResponse
}
