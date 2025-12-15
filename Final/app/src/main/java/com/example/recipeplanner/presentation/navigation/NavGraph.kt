package com.example.recipeplanner.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipeplanner.presentation.auth.AuthScreen
import com.example.recipeplanner.presentation.auth.AuthViewModel
import com.example.recipeplanner.presentation.common.UiEffect
import com.example.recipeplanner.presentation.cookmode.CookModeScreen
import com.example.recipeplanner.presentation.cookmode.CookModeViewModel
import com.example.recipeplanner.presentation.details.RecipeDetailsScreen
import com.example.recipeplanner.presentation.details.RecipeDetailsViewModel
import com.example.recipeplanner.presentation.explore.ExploreScreen
import com.example.recipeplanner.presentation.explore.ExploreViewModel
import com.example.recipeplanner.presentation.favorites.FavoritesScreen
import com.example.recipeplanner.presentation.favorites.FavoritesViewModel
import com.example.recipeplanner.presentation.mealplan.MealPlanScreen
import com.example.recipeplanner.presentation.mealplan.MealPlanViewModel
import com.example.recipeplanner.presentation.settings.SettingsScreen
import com.example.recipeplanner.presentation.settings.SettingsViewModel
import com.example.recipeplanner.presentation.shoppinglist.ShoppingListScreen
import com.example.recipeplanner.presentation.shoppinglist.ShoppingListViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Explore : BottomNavItem(
        NavRoutes.EXPLORE,
        "Explore",
        Icons.Filled.Search,
        Icons.Outlined.Search
    )
    data object Favorites : BottomNavItem(
        NavRoutes.FAVORITES,
        "Favorites",
        Icons.Filled.Favorite,
        Icons.Outlined.FavoriteBorder
    )
    data object MealPlan : BottomNavItem(
        NavRoutes.MEAL_PLAN,
        "Meal Plan",
        Icons.Filled.CalendarMonth,
        Icons.Outlined.CalendarMonth
    )
    data object ShoppingList : BottomNavItem(
        NavRoutes.SHOPPING_LIST,
        "Shopping",
        Icons.Filled.ShoppingCart,
        Icons.Outlined.ShoppingCart
    )
    data object Settings : BottomNavItem(
        NavRoutes.SETTINGS,
        "Settings",
        Icons.Filled.Settings,
        Icons.Outlined.Settings
    )
}

@Composable
fun MainNavGraph(
    isAuthenticated: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) NavRoutes.MAIN else NavRoutes.AUTH,
        modifier = modifier
    ) {
        composable(NavRoutes.AUTH) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            LaunchedEffect(viewModel.effects) {
                viewModel.effects.collectLatest { effect ->
                    when (effect) {
                        is UiEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                        else -> {}
                    }
                }
            }
            
            AuthScreen(
                state = state,
                onEvent = viewModel::onEvent
            )
        }
        
        composable(NavRoutes.MAIN) {
            MainScreen(
                snackbarHostState = snackbarHostState,
                onNavigateToRecipe = { recipeId ->
                    navController.navigate(NavRoutes.recipeDetails(recipeId))
                },
                onNavigateToCookMode = { recipeId ->
                    navController.navigate(NavRoutes.cookMode(recipeId))
                }
            )
        }
        
        composable(
            route = NavRoutes.RECIPE_DETAILS,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            val viewModel: RecipeDetailsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            LaunchedEffect(viewModel.effects) {
                viewModel.effects.collectLatest { effect ->
                    when (effect) {
                        is UiEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                        is UiEffect.Navigate -> {
                            navController.navigate(effect.route)
                        }
                        else -> {}
                    }
                }
            }
            
            RecipeDetailsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = NavRoutes.COOK_MODE,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            val viewModel: CookModeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            LaunchedEffect(viewModel.effects) {
                viewModel.effects.collectLatest { effect ->
                    when (effect) {
                        is UiEffect.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                        else -> {}
                    }
                }
            }
            
            CookModeScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToCookMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Explore,
        BottomNavItem.Favorites,
        BottomNavItem.MealPlan,
        BottomNavItem.ShoppingList,
        BottomNavItem.Settings
    )
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems
            )
        },
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.EXPLORE,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.EXPLORE) {
                val viewModel: ExploreViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                
                LaunchedEffect(viewModel.effects) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is UiEffect.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                            is UiEffect.Navigate -> {
                                onNavigateToRecipe(effect.route.removePrefix("recipe/"))
                            }
                            else -> {}
                        }
                    }
                }
                
                ExploreScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
            
            composable(NavRoutes.FAVORITES) {
                val viewModel: FavoritesViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                
                LaunchedEffect(viewModel.effects) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is UiEffect.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                            is UiEffect.Navigate -> {
                                onNavigateToRecipe(effect.route.removePrefix("recipe/"))
                            }
                            else -> {}
                        }
                    }
                }
                
                FavoritesScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
            
            composable(NavRoutes.MEAL_PLAN) {
                val viewModel: MealPlanViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                
                LaunchedEffect(viewModel.effects) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is UiEffect.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                            is UiEffect.Navigate -> {
                                if (effect.route.startsWith("recipe/")) {
                                    onNavigateToRecipe(effect.route.removePrefix("recipe/"))
                                } else {
                                    navController.navigate(effect.route)
                                }
                            }
                            else -> {}
                        }
                    }
                }
                
                MealPlanScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
            
            composable(NavRoutes.SHOPPING_LIST) {
                val viewModel: ShoppingListViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                
                LaunchedEffect(viewModel.effects) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is UiEffect.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                            else -> {}
                        }
                    }
                }
                
                ShoppingListScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
            
            composable(NavRoutes.SETTINGS) {
                val viewModel: SettingsViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                
                LaunchedEffect(viewModel.effects) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is UiEffect.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                            else -> {}
                        }
                    }
                }
                
                SettingsScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}
