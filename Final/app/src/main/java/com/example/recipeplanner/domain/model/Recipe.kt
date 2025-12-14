package com.example.recipeplanner.domain.model

/**
 * Domain model representing a recipe.
 * Used across the application for recipe-related operations.
 */
data class Recipe(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnailUrl: String,
    val youtubeUrl: String?,
    val ingredients: List<Ingredient>,
    val tags: List<String>,
    val sourceUrl: String?
)

/**
 * Represents a single ingredient with its measurement.
 */
data class Ingredient(
    val name: String,
    val measure: String
) {
    val displayText: String
        get() = if (measure.isNotBlank()) "$measure $name" else name
}
