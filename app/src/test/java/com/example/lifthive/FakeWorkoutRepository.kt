package com.example.lifthive

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWorkoutRepository : WorkoutRepository {
    private val workouts = mutableListOf<Workout>()
    private val workoutsFlow = MutableStateFlow<List<Workout>>(emptyList())

    override fun getWorkouts(): Flow<List<Workout>> {
        return workoutsFlow.map { list -> list.sortedByDescending { it.date } }
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        return workouts.find { it.id == id }
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        val id = if (workout.id == 0L) (workouts.size + 1).toLong() else workout.id
        val newWorkout = workout.copy(id = id)
        
        workouts.removeAll { it.id == id }
        workouts.add(newWorkout)
        workoutsFlow.value = workouts.toList()
        return id
    }

    override suspend fun deleteWorkout(workout: Workout) {
        workouts.removeIf { it.id == workout.id }
        workoutsFlow.value = workouts.toList()
    }

    override suspend fun clearAllWorkouts() {
        workouts.clear()
        workoutsFlow.value = emptyList()
    }

    override suspend fun populateDummyData() {
        // Mock implementation
    }
}
