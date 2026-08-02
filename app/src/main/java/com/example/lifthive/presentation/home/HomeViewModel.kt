package com.example.lifthive.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.usecase.DeleteWorkoutUseCase
import com.example.lifthive.domain.usecase.GetStatsUseCase
import com.example.lifthive.domain.usecase.GetWorkoutsUseCase
import com.example.lifthive.domain.usecase.SaveWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val getStatsUseCase: GetStatsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _recentlyDeletedWorkout = MutableStateFlow<Workout?>(null)

    val state: StateFlow<HomeState> = combine(
        getWorkoutsUseCase(),
        getStatsUseCase(),
        _searchQuery
    ) { workouts, stats, query ->
        val filteredWorkouts = if (query.isBlank()) {
            workouts
        } else {
            workouts.filter { workout ->
                workout.title.contains(query, ignoreCase = true) ||
                        workout.exercises.any { it.name.contains(query, ignoreCase = true) }
            }
        }
        HomeState(
            workouts = filteredWorkouts,
            searchQuery = query,
            stats = stats,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState()
    )

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is HomeEvent.DeleteWorkout -> {
                viewModelScope.launch {
                    _recentlyDeletedWorkout.value = event.workout
                    deleteWorkoutUseCase(event.workout)
                }
            }
            is HomeEvent.UndoDelete -> {
                val deleted = _recentlyDeletedWorkout.value ?: return
                viewModelScope.launch {
                    saveWorkoutUseCase(deleted)
                    _recentlyDeletedWorkout.value = null
                }
            }
        }
    }
}
