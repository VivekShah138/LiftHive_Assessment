package com.example.lifthive.data.local

import androidx.room.*
import com.example.lifthive.data.local.entities.ExerciseEntity
import com.example.lifthive.data.local.entities.WorkoutEntity
import com.example.lifthive.data.local.entities.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutWithExercisesById(id: Long): WorkoutWithExercises?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts")
    suspend fun clearAllWorkouts()

    @Query("DELETE FROM exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: Long)

    @Query("DELETE FROM exercises WHERE workoutId = :workoutId AND id NOT IN (:remainingIds)")
    suspend fun deleteExercisesNotInList(workoutId: Long, remainingIds: List<Long>)
}
