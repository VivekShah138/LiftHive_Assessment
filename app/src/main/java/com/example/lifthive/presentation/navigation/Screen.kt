package com.example.lifthive.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screens {
    
    @Serializable
    object Splash : Screens
    
    @Serializable
    object Home : Screens
    
    @Serializable
    data class AddEditWorkout(val workoutId: Long? = 0L, val templateWorkoutId: Long? = 0L) : Screens
    
    @Serializable
    data class WorkoutDetails(val workoutId: Long) : Screens
    
    @Serializable
    object Stats : Screens
    
    @Serializable
    object Settings : Screens
}
