package com.example.lifthive.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.lifthive.domain.usecase.DeleteWorkoutUseCase
import com.example.lifthive.domain.usecase.GetWorkoutByIdUseCase
import com.example.lifthive.presentation.navigation.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WorkoutDetailsEvent {
    object DeleteWorkout : WorkoutDetailsEvent
}

sealed interface WorkoutDetailsUiEffect {
    object WorkoutDeleted : WorkoutDetailsUiEffect
    data class ShowError(val message: String) : WorkoutDetailsUiEffect
}

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutDetailsState())
    val state: StateFlow<WorkoutDetailsState> = _state.asStateFlow()

    private val _effect = Channel<WorkoutDetailsUiEffect>(Channel.BUFFERED)
    val effect: Flow<WorkoutDetailsUiEffect> = _effect.receiveAsFlow()

    init {
        val route = savedStateHandle.toRoute<Screens.WorkoutDetails>()
        val id = route.workoutId
        loadWorkout(id)
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

    fun onEvent(event: WorkoutDetailsEvent) {
        when (event) {
            is WorkoutDetailsEvent.DeleteWorkout -> {
                val workout = _state.value.workout ?: return
                viewModelScope.launch {
                    deleteWorkoutUseCase(workout)
                    _effect.send(WorkoutDetailsUiEffect.WorkoutDeleted)
                }
            }
        }
    }
}
