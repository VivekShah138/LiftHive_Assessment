package com.example.lifthive.presentation.home

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.model.WorkoutStats

data class HomeState(
    val workouts: List<Workout> = emptyList(),
    val searchQuery: String = "",
    val stats: WorkoutStats? = null,
    val isLoading: Boolean = true
)
