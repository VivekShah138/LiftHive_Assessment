package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.repository.WorkoutRepository
import javax.inject.Inject

class SaveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    class InvalidWorkoutException(message: String) : Exception(message)

    @Throws(InvalidWorkoutException::class)
    suspend operator fun invoke(workout: Workout): Long {
        if (workout.title.trim().isEmpty()) {
            throw InvalidWorkoutException("Workout title cannot be empty.")
        }
        if (workout.exercises.isEmpty()) {
            throw InvalidWorkoutException("A workout must contain at least one exercise.")
        }
        workout.exercises.forEach { exercise ->
            if (exercise.name.trim().isEmpty()) {
                throw InvalidWorkoutException("Exercise name cannot be empty.")
            }
            if (exercise.sets <= 0) {
                throw InvalidWorkoutException("Sets must be greater than zero for ${exercise.name}.")
            }
            if (exercise.reps <= 0) {
                throw InvalidWorkoutException("Reps must be greater than zero for ${exercise.name}.")
            }
            if (exercise.weight < 0) {
                throw InvalidWorkoutException("Weight cannot be negative for ${exercise.name}.")
            }
        }
        return repository.saveWorkout(workout)
    }
}
