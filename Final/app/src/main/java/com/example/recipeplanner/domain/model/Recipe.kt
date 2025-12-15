package com.example.recipeplanner.domain.model

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

data class Ingredient(
    val name: String,
    val measure: String
) {
    val displayText: String
        get() = if (measure.isNotBlank()) "$measure $name" else name
}
