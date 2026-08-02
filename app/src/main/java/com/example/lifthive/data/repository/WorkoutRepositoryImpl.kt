package com.example.lifthive.data.repository

import com.example.lifthive.data.local.WorkoutDao
import com.example.lifthive.data.local.entities.ExerciseEntity
import com.example.lifthive.data.local.entities.WorkoutEntity
import com.example.lifthive.domain.model.Exercise
import com.example.lifthive.domain.model.Workout
import com.example.lifthive.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun getWorkouts(): Flow<List<Workout>> {
        return dao.getWorkoutsWithExercises().map { list ->
            list.map { item ->
                Workout(
                    id = item.workout.id,
                    title = item.workout.title,
                    date = item.workout.date,
                    notes = item.workout.notes,
                    exercises = item.exercises.map { ex ->
                        Exercise(
                            id = ex.id,
                            workoutId = ex.workoutId,
                            name = ex.name,
                            sets = ex.sets,
                            reps = ex.reps,
                            weight = ex.weight
                        )
                    }
                )
            }
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        val item = dao.getWorkoutWithExercisesById(id) ?: return null
        return Workout(
            id = item.workout.id,
            title = item.workout.title,
            date = item.workout.date,
            notes = item.workout.notes,
            exercises = item.exercises.map { ex ->
                Exercise(
                    id = ex.id,
                    workoutId = ex.workoutId,
                    name = ex.name,
                    sets = ex.sets,
                    reps = ex.reps,
                    weight = ex.weight
                )
            }
        )
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        val workoutEntity = WorkoutEntity(
            id = workout.id,
            title = workout.title,
            date = workout.date,
            notes = workout.notes
        )
        val workoutId = dao.insertWorkout(workoutEntity)
        
        // Delete existing exercises if editing
        if (workout.id != 0L) {
            dao.deleteExercisesForWorkout(workout.id)
        }

        val exerciseEntities = workout.exercises.map { ex ->
            ExerciseEntity(
                id = ex.id,
                workoutId = workoutId,
                name = ex.name,
                sets = ex.sets,
                reps = ex.reps,
                weight = ex.weight
            )
        }
        dao.insertExercises(exerciseEntities)
        return workoutId
    }

    override suspend fun deleteWorkout(workout: Workout) {
        val workoutEntity = WorkoutEntity(
            id = workout.id,
            title = workout.title,
            date = workout.date,
            notes = workout.notes
        )
        dao.deleteWorkout(workoutEntity)
    }

    override suspend fun clearAllWorkouts() {
        dao.clearAllWorkouts()
    }

    override suspend fun populateDummyData() {
        dao.clearAllWorkouts()

        val cal = Calendar.getInstance()
        
        // Session 1: Chest & Triceps (2 days ago)
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val w1Id = dao.insertWorkout(
            WorkoutEntity(
                title = "Push Day - Chest Focus",
                date = cal.timeInMillis,
                notes = "Felt strong today. Bench press weights felt light."
            )
        )
        dao.insertExercises(
            listOf(
                ExerciseEntity(workoutId = w1Id, name = "Barbell Bench Press", sets = 4, reps = 8, weight = 80.0),
                ExerciseEntity(workoutId = w1Id, name = "Incline Dumbbell Press", sets = 3, reps = 10, weight = 28.0),
                ExerciseEntity(workoutId = w1Id, name = "Tricep Overhead Extension", sets = 3, reps = 12, weight = 20.0),
                ExerciseEntity(workoutId = w1Id, name = "Lateral Raises", sets = 4, reps = 15, weight = 10.0)
            )
        )

        // Session 2: Legs & Core (Yesterday)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val w2Id = dao.insertWorkout(
            WorkoutEntity(
                title = "Leg Day",
                date = cal.timeInMillis,
                notes = "Focus on deep squats. Hard session!"
            )
        )
        dao.insertExercises(
            listOf(
                ExerciseEntity(workoutId = w2Id, name = "Barbell Squats", sets = 4, reps = 6, weight = 100.0),
                ExerciseEntity(workoutId = w2Id, name = "Romanian Deadlift", sets = 3, reps = 10, weight = 70.0),
                ExerciseEntity(workoutId = w2Id, name = "Leg Press", sets = 3, reps = 12, weight = 160.0),
                ExerciseEntity(workoutId = w2Id, name = "Plank", sets = 3, reps = 60, weight = 0.0)
            )
        )

        // Session 3: Pull Day (Today)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val w3Id = dao.insertWorkout(
            WorkoutEntity(
                title = "Pull Day - Back & Biceps",
                date = cal.timeInMillis,
                notes = "Increased weight on Lat Pulldown. Energy was great."
            )
        )
        dao.insertExercises(
            listOf(
                ExerciseEntity(workoutId = w3Id, name = "Deadlift", sets = 3, reps = 5, weight = 120.0),
                ExerciseEntity(workoutId = w3Id, name = "Barbell Row", sets = 4, reps = 8, weight = 60.0),
                ExerciseEntity(workoutId = w3Id, name = "Lat Pulldown", sets = 3, reps = 10, weight = 55.0),
                ExerciseEntity(workoutId = w3Id, name = "Incline Bicep Curl", sets = 3, reps = 12, weight = 12.0)
            )
        )
    }
}
