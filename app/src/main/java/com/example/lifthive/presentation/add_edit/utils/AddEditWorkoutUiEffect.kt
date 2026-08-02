package com.example.lifthive.presentation.add_edit.utils

sealed interface AddEditWorkoutUiEffect {
    object WorkoutSaved : AddEditWorkoutUiEffect
    data class ShowToast(val message: String) : AddEditWorkoutUiEffect
}
