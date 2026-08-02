package com.example.lifthive.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    
    object AddEditWorkout : Screen("add_edit_workout?workoutId={workoutId}") {
        fun passWorkoutId(workoutId: Long): String {
            return "add_edit_workout?workoutId=$workoutId"
        }
    }
    
    object WorkoutDetails : Screen("workout_details/{workoutId}") {
        fun passWorkoutId(workoutId: Long): String {
            return "workout_details/$workoutId"
        }
    }
    
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}
