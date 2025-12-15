package com.example.recipeplanner.presentation.navigation

object NavRoutes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val MAIN = "main"
    const val EXPLORE = "explore"
    const val RECIPE_DETAILS = "recipe/{recipeId}"
    const val FAVORITES = "favorites"
    const val MEAL_PLAN = "meal_plan"
    const val SHOPPING_LIST = "shopping_list"
    const val SETTINGS = "settings"
    const val COOK_MODE = "cook_mode/{recipeId}"

    fun recipeDetails(recipeId: String) = "recipe/$recipeId"
    fun cookMode(recipeId: String) = "cook_mode/$recipeId"
}

enum class BottomNavDestination(
    val route: String,
    val title: String,
    val icon: String
) {
    EXPLORE(NavRoutes.EXPLORE, "Explore", "search"),
    FAVORITES(NavRoutes.FAVORITES, "Favorites", "favorite"),
    MEAL_PLAN(NavRoutes.MEAL_PLAN, "Meal Plan", "calendar_today"),
    SHOPPING_LIST(NavRoutes.SHOPPING_LIST, "Shopping", "shopping_cart"),
    SETTINGS(NavRoutes.SETTINGS, "Settings", "settings")
}
