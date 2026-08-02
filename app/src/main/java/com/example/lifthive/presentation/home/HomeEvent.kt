package com.example.lifthive.presentation.home

import com.example.lifthive.domain.model.Workout

sealed interface HomeEvent {
    data class SearchQueryChanged(val query: String) : HomeEvent
    data class DeleteWorkout(val workout: Workout) : HomeEvent
    object UndoDelete : HomeEvent
}
