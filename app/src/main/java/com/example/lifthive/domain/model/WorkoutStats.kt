package com.example.lifthive.domain.model

data class WorkoutStats(
    val totalWorkouts: Int,
    val totalWeightLifted: Double,
    val mostFrequentExercise: String,
    val lastWorkoutsVolume: List<Pair<String, Double>> // Represents (Workout Title, Total Volume in kg/lbs)
)
