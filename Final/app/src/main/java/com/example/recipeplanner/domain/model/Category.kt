package com.example.recipeplanner.domain.model

/**
 * Represents a recipe category for filtering.
 */
data class Category(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val description: String
)
