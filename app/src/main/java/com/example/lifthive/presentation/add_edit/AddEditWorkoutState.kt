package com.example.lifthive.presentation.add_edit

import com.example.lifthive.domain.model.Exercise

data class AddEditWorkoutState(
    val title: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,


    val exerciseName: String = "",
    val exerciseSets: String = "",
    val exerciseReps: String = "",
    val exerciseWeight: String = ""
)
