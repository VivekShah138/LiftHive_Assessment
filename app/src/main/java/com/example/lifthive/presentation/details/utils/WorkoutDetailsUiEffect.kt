package com.example.lifthive.presentation.details.utils

sealed interface WorkoutDetailsUiEffect {
    object WorkoutDeleted : WorkoutDetailsUiEffect
    data class ShowError(val message: String) : WorkoutDetailsUiEffect
}