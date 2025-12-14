# Recipe Planner

A modern Android application for meal planning, recipe discovery, and shopping list management. Built with Kotlin, Jetpack Compose, and Clean Architecture.

![Android](https://img.shields.io/badge/Android-26%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-purple)

## Features

### Core Features
- **Explore Recipes**: Browse and search recipes from TheMealDB API with category filtering
- **Recipe Details**: View ingredients, instructions, and add to favorites or meal plan
- **My Favorites**: Save recipes for quick access with offline support
- **Meal Plan**: Weekly meal planning with breakfast, lunch, and dinner slots
- **Shopping List**: Auto-generated shopping list from meal plan with smart ingredient grouping
- **Settings**: Light/Dark/System theme support

### Creative Feature: Cook Mode
- **Step-by-step cooking interface** with large, readable text
- **Built-in timer** with quick presets (1, 5, 10 minutes)
- **Swipe navigation** between steps
- **Quick access to ingredients** via bottom sheet

## Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── data/                          # Data Layer
│   ├── local/                     # Room Database
│   │   ├── dao/                   # Data Access Objects
│   │   ├── entity/                # Database Entities
│   │   └── RecipePlannerDatabase.kt
│   ├── remote/                    # Network
│   │   ├── api/                   # Retrofit API
│   │   └── dto/                   # Data Transfer Objects
│   ├── mapper/                    # Entity ↔ Domain mappers
│   └── repository/                # Repository Implementations
│
├── domain/                        # Domain Layer
│   ├── model/                     # Domain Models
│   ├── repository/                # Repository Interfaces
│   ├── usecase/                   # Use Cases
│   │   ├── auth/
│   │   ├── favorite/
│   │   ├── mealplan/
│   │   ├── recipe/
│   │   └── shoppinglist/
│   └── util/                      # AppResult, AppError
│
├── presentation/                  # Presentation Layer
│   ├── auth/                      # Login/Register
│   ├── explore/                   # Recipe Search
│   ├── details/                   # Recipe Details
│   ├── favorites/                 # Favorites List
│   ├── mealplan/                  # Weekly Meal Plan
│   ├── shoppinglist/              # Shopping List
│   ├── settings/                  # App Settings
│   ├── cookmode/                  # Cook Mode Feature
│   ├── components/                # Reusable UI Components
│   ├── navigation/                # Navigation Graph
│   └── theme/                     # Material 3 Theme
│
└── di/                            # Dependency Injection (Hilt)
```

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.0 |
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Local Storage | Room + DataStore |
| Networking | Retrofit + OkHttp + Kotlinx Serialization |
| Backend | Supabase (Auth + PostgreSQL) |
| Image Loading | Coil |
| Async | Kotlin Coroutines + Flow |
| Testing | JUnit + MockK + Turbine |
| Logging | Timber |

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Configuration

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Final
   ```

2. **Create Supabase Project**
   - Go to [supabase.com](https://supabase.com) and create a new project
   - Copy your project URL and anon key

3. **Configure local.properties**
   
   Add the following to your `local.properties` file (create if it doesn't exist):
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key-here
   ```

4. **Set up Supabase Database**
   
   Run the SQL from `supabase/setup.sql` in your Supabase SQL Editor.

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## Supabase Database Setup

See `supabase/setup.sql` for complete database schema including:
- Tables: `favorites`, `meal_plan_entries`, `shopping_list_items`
- Row Level Security (RLS) policies
- Required indexes

## API

This app uses [TheMealDB API](https://www.themealdb.com/api.php) which is free and doesn't require an API key for basic usage.

**Endpoints used:**
- `search.php?s={query}` - Search meals by name
- `lookup.php?i={id}` - Get meal details
- `categories.php` - Get all categories
- `filter.php?c={category}` - Filter by category
- `random.php` - Get random meal

## Demo Scenarios

### 1. Authentication Flow
1. Launch app → Auth screen appears
2. Register with email/password
3. Login with credentials
4. Logout from Settings

### 2. Recipe Discovery
1. Browse recipes on Explore tab
2. Use search bar to find specific recipes
3. Filter by category using chips
4. Tap recipe card to view details

### 3. Favorites Management
1. From recipe details, tap heart icon
2. Navigate to Favorites tab to see saved recipes
3. Works offline after initial sync

### 4. Meal Planning
1. From recipe details, tap "Add to Plan"
2. Select date and meal type
3. View weekly plan in Meal Plan tab
4. Navigate between weeks

### 5. Shopping List Generation
1. Add recipes to meal plan
2. Go to Shopping List tab
3. Tap "Generate" button
4. Ingredients are aggregated and categorized
5. Check off items as you shop

### 6. Cook Mode
1. From recipe details, tap "Cook Mode"
2. Navigate through steps with Next/Previous
3. Use timer for timed steps
4. Access ingredients via top-right button

## Project Structure

```
Final/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/recipeplanner/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Unit Tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml           # Version Catalog
├── supabase/
│   └── setup.sql                    # Database Schema
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Testing

Run unit tests:
```bash
./gradlew test
```

Key test files:
- `SignInUseCaseTest.kt` - Auth validation tests
- `ToggleFavoriteUseCaseTest.kt` - Favorite toggle tests
- `GenerateShoppingListUseCaseTest.kt` - Shopping list generation tests

## Code Quality

- Follows Kotlin coding conventions
- KDoc comments on public APIs
- State hoisting in Compose
- Sealed classes for UI states and events
- Flow-based reactive data
- Error handling with AppResult/AppError

## Offline Support

The app uses Room as offline cache for:
- ✅ Cached recipes (for previously viewed recipes)
- ✅ Favorites list
- ✅ Meal plan entries
- ✅ Shopping list items

Data syncs with Supabase when online.

## Screenshots

| Explore | Recipe Details | Meal Plan |
|---------|---------------|-----------|
| ![Explore](screenshots/explore.jpg) | ![Recipe Details](screenshots/recipe_details.jpg) | ![Meal Plan](screenshots/meal_plan.jpg) |

| Shopping List | Cook Mode | Settings |
|---------------|-----------|----------|
| ![Shopping List](screenshots/shopping_list.jpg) | ![Cook Mode](screenshots/cook_mode.jpg) | ![Settings](screenshots/settings.jpg) |

## License

This project is created for educational purposes.

## Author

Final project for Android Development course.
