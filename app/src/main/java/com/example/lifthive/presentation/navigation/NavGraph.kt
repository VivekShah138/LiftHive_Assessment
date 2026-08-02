package com.example.lifthive.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController, mainViewModel = mainViewModel)
        }
        
        composable(
            route = Screen.AddEditWorkout.route,
            arguments = listOf(
                navArgument("workoutId") {
                    type = NavType.StringType
                    defaultValue = "0"
                    nullable = true
                }
            )
        ) {
            AddEditWorkoutScreen(navController = navController)
        }
        
        composable(
            route = Screen.WorkoutDetails.route,
            arguments = listOf(
                navArgument("workoutId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
            WorkoutDetailsScreen(navController = navController, workoutId = workoutId)
        }
        
        composable(route = Screen.Stats.route) {
            StatsScreen(navController = navController)
        }
        
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController, mainViewModel = mainViewModel)
        }
    }
}
