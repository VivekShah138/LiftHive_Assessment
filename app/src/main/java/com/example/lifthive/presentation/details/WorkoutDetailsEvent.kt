package com.example.lifthive.presentation.details

sealed interface WorkoutDetailsEvent {
    object DeleteWorkout : WorkoutDetailsEvent
}