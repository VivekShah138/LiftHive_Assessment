package com.example.lifthive.presentation.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.lifthive.domain.model.Exercise
import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.usecase.GetWorkoutByIdUseCase
import com.example.lifthive.domain.usecase.SaveWorkoutUseCase
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

sealed interface AddEditWorkoutUiEffect {
    object WorkoutSaved : AddEditWorkoutUiEffect
    data class ShowToast(val message: String) : AddEditWorkoutUiEffect
}

@HiltViewModel
class AddEditWorkoutViewModel @Inject constructor(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditWorkoutState())
    val state: StateFlow<AddEditWorkoutState> = _state.asStateFlow()

    private val _effect = Channel<AddEditWorkoutUiEffect>(Channel.BUFFERED)
    val effect: Flow<AddEditWorkoutUiEffect> = _effect.receiveAsFlow()

    private var currentWorkoutId: Long = 0L

    init {
        val route = savedStateHandle.toRoute<Screens.AddEditWorkout>()
        val workoutIdArg = route.workoutId
        val templateWorkoutIdArg = route.templateWorkoutId
        
        if (workoutIdArg != null && workoutIdArg != 0L) {
            currentWorkoutId = workoutIdArg
            loadWorkout(workoutIdArg)
        } else if (templateWorkoutIdArg != null && templateWorkoutIdArg != 0L) {
            currentWorkoutId = 0L
            loadWorkoutAsTemplate(templateWorkoutIdArg)
        }
    }

    private fun loadWorkout(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val workout = getWorkoutByIdUseCase(id)
            if (workout != null) {
                _state.update {
                    it.copy(
                        title = workout.title,
                        date = workout.date,
                        notes = workout.notes,
                        exercises = workout.exercises,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Workout not found") }
            }
        }
    }

    private fun loadWorkoutAsTemplate(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val workout = getWorkoutByIdUseCase(id)
            if (workout != null) {
                val templatedExercises = workout.exercises.map { exercise ->
                    exercise.copy(id = 0L, workoutId = 0L)
                }
                _state.update {
                    it.copy(
                        title = workout.title,
                        date = System.currentTimeMillis(),
                        notes = workout.notes,
                        exercises = templatedExercises,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Template workout not found") }
            }
        }
    }

    fun onEvent(event: AddEditWorkoutEvent) {
        when (event) {
            is AddEditWorkoutEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddEditWorkoutEvent.NotesChanged -> {
                _state.update { it.copy(notes = event.notes) }
            }
            is AddEditWorkoutEvent.DateChanged -> {
                _state.update { it.copy(date = event.date) }
            }
            is AddEditWorkoutEvent.ExerciseNameChanged -> {
                _state.update { it.copy(exerciseName = event.name) }
            }
            is AddEditWorkoutEvent.ExerciseSetsChanged -> {
                _state.update { it.copy(exerciseSets = event.sets) }
            }
            is AddEditWorkoutEvent.ExerciseRepsChanged -> {
                _state.update { it.copy(exerciseReps = event.reps) }
            }
            is AddEditWorkoutEvent.ExerciseWeightChanged -> {
                _state.update { it.copy(exerciseWeight = event.weight) }
            }
            is AddEditWorkoutEvent.AddExercise -> {
                addExercise()
            }
            is AddEditWorkoutEvent.RemoveExercise -> {
                _state.update {
                    val list = it.exercises.toMutableList()
                    if (event.index in list.indices) {
                        list.removeAt(event.index)
                    }
                    it.copy(exercises = list)
                }
            }
            is AddEditWorkoutEvent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
            is AddEditWorkoutEvent.SaveWorkout -> {
                saveWorkout()
            }
        }
    }

    private fun addExercise() {
        val name = _state.value.exerciseName.trim()
        val sets = _state.value.exerciseSets.toIntOrNull() ?: 0
        val reps = _state.value.exerciseReps.toIntOrNull() ?: 0
        val weight = _state.value.exerciseWeight.toDoubleOrNull() ?: 0.0

        if (name.isEmpty()) {
            _state.update { it.copy(errorMessage = "Exercise name cannot be empty") }
            return
        }
        if (sets <= 0) {
            _state.update { it.copy(errorMessage = "Sets must be greater than zero") }
            return
        }
        if (reps <= 0) {
            _state.update { it.copy(errorMessage = "Reps must be greater than zero") }
            return
        }
        if (weight < 0.0) {
            _state.update { it.copy(errorMessage = "Weight cannot be negative") }
            return
        }

        val newExercise = Exercise(
            id = 0,
            workoutId = currentWorkoutId,
            name = name,
            sets = sets,
            reps = reps,
            weight = weight
        )

        _state.update {
            it.copy(
                exercises = it.exercises + newExercise,
                exerciseName = "",
                exerciseSets = "",
                exerciseReps = "",
                exerciseWeight = "",
                errorMessage = null
            )
        }
    }

    private fun saveWorkout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val workout = Workout(
                id = currentWorkoutId,
                title = _state.value.title.trim(),
                date = _state.value.date,
                notes = _state.value.notes.trim(),
                exercises = _state.value.exercises
            )
            try {
                saveWorkoutUseCase(workout)
                _state.update { it.copy(isLoading = false, isSaved = true) }
                _effect.send(AddEditWorkoutUiEffect.WorkoutSaved)
            } catch (e: SaveWorkoutUseCase.InvalidWorkoutException) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Could not save workout") }
            }
        }
    }
}
