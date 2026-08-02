package com.example.lifthive.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifthive.domain.usecase.DeleteWorkoutUseCase
import com.example.lifthive.domain.usecase.GetWorkoutByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutDetailsState())
    val state: StateFlow<WorkoutDetailsState> = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<String>("workoutId")?.toLongOrNull()
            ?: savedStateHandle.get<Long>("workoutId")
            
        if (id != null) {
            loadWorkout(id)
        } else {
            _state.update { it.copy(error = "Workout ID not specified") }
        }
    }

    private fun loadWorkout(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val workout = getWorkoutByIdUseCase(id)
            if (workout != null) {
                _state.update { it.copy(workout = workout, isLoading = false) }
            } else {
                _state.update { it.copy(error = "Workout not found", isLoading = false) }
            }
        }
    }

    fun deleteWorkout(onSuccess: () -> Unit) {
        val workout = _state.value.workout ?: return
        viewModelScope.launch {
            deleteWorkoutUseCase(workout)
            onSuccess()
        }
    }
}
