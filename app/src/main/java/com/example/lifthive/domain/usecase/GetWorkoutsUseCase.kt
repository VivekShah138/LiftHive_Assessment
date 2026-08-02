package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> {
        return repository.getWorkouts()
    }
}
