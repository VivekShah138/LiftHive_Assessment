package com.example.lifthive.domain.model

data class WorkoutStats(
    val totalWorkouts: Int,
    val totalWeightLifted: Double,
    val mostFrequentExercise: String,
    val lastWorkoutsVolume: List<Pair<String, Double>>,   // (label, volume) last 6 sessions
    val workoutDates: List<Long> = emptyList(),
    // New rich analytics fields
    val currentStreak: Int = 0,                          // consecutive days up to today
    val longestStreak: Int = 0,                          // all-time best streak
    val avgVolumePerSession: Double = 0.0,               // mean kg per session
    val bestSessionVolume: Double = 0.0,                 // all-time single-session PR
    val bestSessionLabel: String = "",                   // formatted date of that PR
    val weeklyVolumes: List<Pair<String, Double>> = emptyList(), // (weekLabel, volume) last 4 weeks
    val topExercises: List<Pair<String, Int>> = emptyList()      // (name, count) top 5
)
