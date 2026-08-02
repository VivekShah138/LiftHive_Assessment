package com.example.lifthive.domain.repository

import com.example.lifthive.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getWorkouts(): Flow<List<Workout>>
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun deleteWorkout(workout: Workout)
    suspend fun clearAllWorkouts()
    suspend fun populateDummyData()
}
