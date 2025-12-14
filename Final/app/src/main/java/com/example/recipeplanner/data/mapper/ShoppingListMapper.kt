package com.example.recipeplanner.data.mapper

import com.example.recipeplanner.data.local.entity.ShoppingListEntity
import com.example.recipeplanner.data.remote.dto.ShoppingListItemDto
import com.example.recipeplanner.domain.model.IngredientCategory
import com.example.recipeplanner.domain.model.ShoppingListItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Maps ShoppingListItemDto from Supabase to domain ShoppingListItem model.
 */
fun ShoppingListItemDto.toShoppingListItem(): ShoppingListItem {
    return ShoppingListItem(
        id = id,
        userId = userId,
        ingredientName = ingredientName,
        quantity = quantity,
        category = try {
            IngredientCategory.valueOf(category.uppercase())
        } catch (e: Exception) {
            IngredientCategory.OTHER
        },
        isChecked = isChecked,
        recipeIds = recipeIds
    )
}

/**
 * Maps ShoppingListEntity from local storage to domain ShoppingListItem model.
 */
fun ShoppingListEntity.toShoppingListItem(): ShoppingListItem {
    val recipeIdsList = try {
        json.decodeFromString<List<String>>(recipeIdsJson)
    } catch (e: Exception) {
        emptyList()
    }

    return ShoppingListItem(
        id = id,
        userId = userId,
        ingredientName = ingredientName,
        quantity = quantity,
        category = try {
            IngredientCategory.valueOf(category.uppercase())
        } catch (e: Exception) {
            IngredientCategory.OTHER
        },
        isChecked = isChecked,
        recipeIds = recipeIdsList
    )
}

/**
 * Maps domain ShoppingListItem to ShoppingListEntity for local storage.
 */
fun ShoppingListItem.toEntity(): ShoppingListEntity {
    return ShoppingListEntity(
        id = id,
        userId = userId,
        ingredientName = ingredientName,
        quantity = quantity,
        category = category.name,
        isChecked = isChecked,
        recipeIdsJson = json.encodeToString(recipeIds)
    )
}

/**
 * Maps domain ShoppingListItem to ShoppingListItemDto for Supabase.
 */
fun ShoppingListItem.toDto(): ShoppingListItemDto {
    return ShoppingListItemDto(
        id = id,
        userId = userId,
        ingredientName = ingredientName,
        quantity = quantity,
        category = category.name,
        isChecked = isChecked,
        recipeIds = recipeIds
    )
}
