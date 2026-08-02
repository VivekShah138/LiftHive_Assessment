package com.example.lifthive.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.lifthive.presentation.MainViewModel
import com.example.lifthive.presentation.add_edit.AddEditWorkoutScreenRoot
import com.example.lifthive.presentation.details.WorkoutDetailsScreenRoot
import com.example.lifthive.presentation.home.HomeScreenRoot
import com.example.lifthive.presentation.settings.SettingsScreenRoot
import com.example.lifthive.presentation.splash.SplashScreen
import com.example.lifthive.presentation.stats.StatsScreenRoot

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Splash
    ) {
        composable<Screens.Splash> {
            SplashScreen(navController = navController)
        }
        
        composable<Screens.Home> {
            HomeScreenRoot(navController = navController)
        }
        
        composable<Screens.AddEditWorkout> {
            AddEditWorkoutScreenRoot(navController = navController)
        }
        
        composable<Screens.WorkoutDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<Screens.WorkoutDetails>()
            WorkoutDetailsScreenRoot(navController = navController, workoutId = route.workoutId)
        }
        
        composable<Screens.Stats> {
            StatsScreenRoot(navController = navController)
        }
        
        composable<Screens.Settings> {
            SettingsScreenRoot(navController = navController, mainViewModel = mainViewModel)
        }
    }
}
