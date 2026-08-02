package com.example.lifthive.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.lifthive.presentation.MainViewModel
import com.example.lifthive.presentation.add_edit.AddEditWorkoutScreen
import com.example.lifthive.presentation.details.WorkoutDetailsScreen
import com.example.lifthive.presentation.home.HomeScreen
import com.example.lifthive.presentation.settings.SettingsScreen
import com.example.lifthive.presentation.splash.SplashScreen
import com.example.lifthive.presentation.stats.StatsScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        composable<SplashRoute> {
            SplashScreen(navController = navController)
        }
        
        composable<HomeRoute> {
            HomeScreen(navController = navController, mainViewModel = mainViewModel)
        }
        
        composable<AddEditWorkoutRoute> {
            AddEditWorkoutScreen(navController = navController)
        }
        
        composable<WorkoutDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<WorkoutDetailsRoute>()
            WorkoutDetailsScreen(navController = navController, workoutId = route.workoutId)
        }
        
        composable<StatsRoute> {
            StatsScreen(navController = navController)
        }
        
        composable<SettingsRoute> {
            SettingsScreen(navController = navController, mainViewModel = mainViewModel)
        }
    }
}
