package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.repository.WorkoutRepository
import javax.inject.Inject

class DeleteWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) {
        repository.deleteWorkout(workout)
    }
}
