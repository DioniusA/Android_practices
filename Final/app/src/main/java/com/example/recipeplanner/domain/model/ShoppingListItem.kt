package com.example.recipeplanner.domain.model

/**
 * Represents an item in the shopping list.
 */
data class ShoppingListItem(
    val id: String,
    val userId: String,
    val ingredientName: String,
    val quantity: String,
    val category: IngredientCategory,
    val isChecked: Boolean,
    val recipeIds: List<String>
)

/**
 * Categories for organizing shopping list items.
 */
enum class IngredientCategory {
    PRODUCE,
    DAIRY,
    MEAT,
    SEAFOOD,
    BAKERY,
    PANTRY,
    FROZEN,
    BEVERAGES,
    SPICES,
    OTHER;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }

    companion object {
        /**
         * Attempts to categorize an ingredient by its name.
         */
        fun fromIngredientName(name: String): IngredientCategory {
            val lowerName = name.lowercase()
            return when {
                lowerName.containsAny("lettuce", "tomato", "onion", "garlic", "pepper", "carrot", 
                    "potato", "vegetable", "fruit", "apple", "banana", "lemon", "lime", "orange",
                    "celery", "cucumber", "spinach", "broccoli", "mushroom") -> PRODUCE
                lowerName.containsAny("milk", "cheese", "cream", "butter", "yogurt", "egg") -> DAIRY
                lowerName.containsAny("chicken", "beef", "pork", "lamb", "bacon", "sausage", 
                    "meat", "turkey", "duck") -> MEAT
                lowerName.containsAny("fish", "salmon", "tuna", "shrimp", "prawn", "crab", 
                    "lobster", "seafood", "cod") -> SEAFOOD
                lowerName.containsAny("bread", "baguette", "roll", "croissant", "pastry") -> BAKERY
                lowerName.containsAny("ice cream", "frozen") -> FROZEN
                lowerName.containsAny("water", "juice", "soda", "wine", "beer", "coffee", "tea") -> BEVERAGES
                lowerName.containsAny("salt", "pepper", "cumin", "paprika", "oregano", "basil",
                    "thyme", "rosemary", "cinnamon", "nutmeg", "spice", "herb") -> SPICES
                lowerName.containsAny("flour", "sugar", "rice", "pasta", "oil", "vinegar", 
                    "sauce", "can", "stock", "broth", "bean", "lentil", "noodle") -> PANTRY
                else -> OTHER
            }
        }

        private fun String.containsAny(vararg keywords: String): Boolean =
            keywords.any { this.contains(it) }
    }
}
