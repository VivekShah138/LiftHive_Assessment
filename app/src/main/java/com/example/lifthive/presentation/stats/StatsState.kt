package com.example.lifthive.presentation.stats

import com.example.lifthive.domain.model.WorkoutStats

data class StatsState(
    val stats: WorkoutStats? = null,
    val isLoading: Boolean = true
)
