package com.example.lifthive.presentation.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifthive.domain.model.Exercise
import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.usecase.GetWorkoutByIdUseCase
import com.example.lifthive.domain.usecase.SaveWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditWorkoutViewModel @Inject constructor(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditWorkoutState())
    val state: StateFlow<AddEditWorkoutState> = _state.asStateFlow()

    private var currentWorkoutId: Long = 0L

    init {
        // Compose Navigation may pass arguments as String under the hood for query params
        val workoutIdArg = savedStateHandle.get<String>("workoutId")?.toLongOrNull() 
            ?: savedStateHandle.get<Long>("workoutId")
            
        if (workoutIdArg != null && workoutIdArg != 0L) {
            currentWorkoutId = workoutIdArg
            loadWorkout(workoutIdArg)
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

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun onNotesChange(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun onDateChange(date: Long) {
        _state.update { it.copy(date = date) }
    }

    fun onExerciseNameChange(name: String) {
        _state.update { it.copy(exerciseName = name) }
    }

    fun onExerciseSetsChange(sets: String) {
        _state.update { it.copy(exerciseSets = sets) }
    }

    fun onExerciseRepsChange(reps: String) {
        _state.update { it.copy(exerciseReps = reps) }
    }

    fun onExerciseWeightChange(weight: String) {
        _state.update { it.copy(exerciseWeight = weight) }
    }

    fun addExercise() {
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

    fun removeExercise(index: Int) {
        _state.update {
            val list = it.exercises.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(exercises = list)
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun saveWorkout() {
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
            } catch (e: SaveWorkoutUseCase.InvalidWorkoutException) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Could not save workout") }
            }
        }
    }
}
