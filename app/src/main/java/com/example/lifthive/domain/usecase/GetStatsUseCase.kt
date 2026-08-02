package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.model.WorkoutStats
import com.example.lifthive.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<WorkoutStats> {
        return repository.getWorkouts().map { workouts ->
            val totalWorkouts = workouts.size
            
            var totalWeightLifted = 0.0
            val exerciseCounts = mutableMapOf<String, Int>()
            
            workouts.forEach { workout ->
                workout.exercises.forEach { exercise ->
                    val volume = exercise.sets * exercise.reps * exercise.weight
                    totalWeightLifted += volume
                    
                    val name = exercise.name.trim()
                    if (name.isNotEmpty()) {
                        exerciseCounts[name] = exerciseCounts.getOrDefault(name, 0) + 1
                    }
                }
            }
            
            val mostFrequentExercise = exerciseCounts.maxByOrNull { it.value }?.key ?: "None"
            
            // Generate list of volumes for the last 6 workouts, chronologically (left to right)
            val last6Workouts = workouts.take(6).reversed()
            val lastWorkoutsVolume = last6Workouts.map { workout ->
                val volume = workout.exercises.sumOf { it.sets * it.reps * it.weight }
                // Truncate name if too long for chart label
                val title = if (workout.title.length > 10) workout.title.take(8) + ".." else workout.title
                Pair(title, volume)
            }
            
            WorkoutStats(
                totalWorkouts = totalWorkouts,
                totalWeightLifted = totalWeightLifted,
                mostFrequentExercise = mostFrequentExercise,
                lastWorkoutsVolume = lastWorkoutsVolume
            )
        }
    }
}
