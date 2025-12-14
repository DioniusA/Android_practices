package com.example.recipeplanner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response wrapper for categories from TheMealDB.
 */
@Serializable
data class CategoriesResponse(
    @SerialName("categories")
    val categories: List<CategoryDto>? = null
)

/**
 * DTO for a category from TheMealDB API.
 */
@Serializable
data class CategoryDto(
    @SerialName("idCategory")
    val idCategory: String,

    @SerialName("strCategory")
    val strCategory: String,

    @SerialName("strCategoryThumb")
    val strCategoryThumb: String,

    @SerialName("strCategoryDescription")
    val strCategoryDescription: String
)
