package com.example.lifthive.presentation.details

import com.example.lifthive.domain.model.Workout

data class WorkoutDetailsState(
    val workout: Workout? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
