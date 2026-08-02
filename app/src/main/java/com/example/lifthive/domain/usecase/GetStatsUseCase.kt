package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.model.WorkoutStats
import com.example.lifthive.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                val title = sdf.format(Date(workout.date))
                Pair(title, volume)
            }
            
            val workoutDates = workouts.map { it.date }

            WorkoutStats(
                totalWorkouts = totalWorkouts,
                totalWeightLifted = totalWeightLifted,
                mostFrequentExercise = mostFrequentExercise,
                lastWorkoutsVolume = lastWorkoutsVolume,
                workoutDates = workoutDates
            )
        }
    }
}
