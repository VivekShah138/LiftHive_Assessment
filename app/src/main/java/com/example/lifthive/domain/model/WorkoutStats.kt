package com.example.lifthive.domain.model

data class WorkoutStats(
    val totalWorkouts: Int,
    val totalWeightLifted: Double,
    val mostFrequentExercise: String,
    val lastWorkoutsVolume: List<Pair<String, Double>>,
    val workoutDates: List<Long> = emptyList()
)
