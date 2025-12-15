package com.example.recipeplanner.data.mapper

import com.example.recipeplanner.data.local.entity.RecipeEntity
import com.example.recipeplanner.data.remote.dto.CategoryDto
import com.example.recipeplanner.data.remote.dto.MealDto
import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.Recipe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun MealDto.toRecipe(): Recipe {
    val ingredientPairs = getIngredientPairs()
    val ingredients = ingredientPairs.map { (name, measure) ->
        Ingredient(name = name, measure = measure.trim())
    }

    return Recipe(
        id = idMeal,
        name = strMeal,
        category = strCategory ?: "",
        area = strArea ?: "",
        instructions = strInstructions ?: "",
        thumbnailUrl = strMealThumb ?: "",
        youtubeUrl = strYoutube?.takeIf { it.isNotBlank() },
        ingredients = ingredients,
        tags = strTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        sourceUrl = strSource?.takeIf { it.isNotBlank() }
    )
}

fun CategoryDto.toCategory(): Category {
    return Category(
        id = idCategory,
        name = strCategory,
        thumbnailUrl = strCategoryThumb,
        description = strCategoryDescription
    )
}

fun Recipe.toEntity(): RecipeEntity {
    val json = Json { ignoreUnknownKeys = true }
    return RecipeEntity(
        id = id,
        name = name,
        category = category,
        area = area,
        instructions = instructions,
        thumbnailUrl = thumbnailUrl,
        youtubeUrl = youtubeUrl,
        ingredientsJson = json.encodeToString(ingredients.map { IngredientJson(it.name, it.measure) }),
        tagsJson = json.encodeToString(tags),
        sourceUrl = sourceUrl
    )
}

fun RecipeEntity.toRecipe(): Recipe {
    val json = Json { ignoreUnknownKeys = true }
    val ingredientsList = try {
        json.decodeFromString<List<IngredientJson>>(ingredientsJson)
            .map { Ingredient(it.name, it.measure) }
    } catch (e: Exception) {
        emptyList()
    }
    val tagsList = try {
        json.decodeFromString<List<String>>(tagsJson)
    } catch (e: Exception) {
        emptyList()
    }

    return Recipe(
        id = id,
        name = name,
        category = category,
        area = area,
        instructions = instructions,
        thumbnailUrl = thumbnailUrl,
        youtubeUrl = youtubeUrl,
        ingredients = ingredientsList,
        tags = tagsList,
        sourceUrl = sourceUrl
    )
}

@kotlinx.serialization.Serializable
private data class IngredientJson(
    val name: String,
    val measure: String
)
