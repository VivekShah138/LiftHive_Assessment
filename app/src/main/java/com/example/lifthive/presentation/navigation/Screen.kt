package com.example.lifthive.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object HomeRoute

@Serializable
data class AddEditWorkoutRoute(val workoutId: Long? = 0L)

@Serializable
data class WorkoutDetailsRoute(val workoutId: Long)

@Serializable
object StatsRoute

@Serializable
object SettingsRoute
