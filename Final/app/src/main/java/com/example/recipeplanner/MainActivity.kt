package com.example.recipeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipeplanner.domain.repository.ThemeMode
import com.example.recipeplanner.presentation.MainViewModel
import com.example.recipeplanner.presentation.navigation.MainNavGraph
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle(initialValue = false)
            
            RecipePlannerTheme(themeMode = themeMode) {
                MainNavGraph(isAuthenticated = isAuthenticated)
            }
        }
    }
}
