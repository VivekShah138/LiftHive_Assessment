package com.example.lifthive.domain.model

data class Exercise(
    val id: Long = 0,
    val workoutId: Long = 0,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double
)
