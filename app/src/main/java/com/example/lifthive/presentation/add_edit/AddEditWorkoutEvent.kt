package com.example.lifthive.presentation.add_edit

sealed interface AddEditWorkoutEvent {
    data class TitleChanged(val title: String) : AddEditWorkoutEvent
    data class NotesChanged(val notes: String) : AddEditWorkoutEvent
    data class DateChanged(val date: Long) : AddEditWorkoutEvent
    data class ExerciseNameChanged(val name: String) : AddEditWorkoutEvent
    data class ExerciseSetsChanged(val sets: String) : AddEditWorkoutEvent
    data class ExerciseRepsChanged(val reps: String) : AddEditWorkoutEvent
    data class ExerciseWeightChanged(val weight: String) : AddEditWorkoutEvent
    object AddExercise : AddEditWorkoutEvent
    data class RemoveExercise(val index: Int) : AddEditWorkoutEvent
    object ClearError : AddEditWorkoutEvent
    object SaveWorkout : AddEditWorkoutEvent
}