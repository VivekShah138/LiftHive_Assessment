package com.example.lifthive.data.repository

import com.example.lifthive.data.local.room.WorkoutDao
import com.example.lifthive.data.local.room.entities.ExerciseEntity
import com.example.lifthive.data.local.room.entities.WorkoutEntity
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
        val workoutId = dao.upsertWorkout(workoutEntity)

        if (workout.id != 0L) {
            val remainingIds = workout.exercises.map { it.id }.filter { it != 0L }
            if (remainingIds.isNotEmpty()) {
                dao.deleteExercisesNotInList(workout.id, remainingIds)
            } else {
                dao.deleteExercisesForWorkout(workout.id)
            }
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
        dao.upsertExercises(exerciseEntities)
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

        fun dayMillis(daysAgo: Int): Long {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            c.set(Calendar.HOUR_OF_DAY, 8)
            c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
            return c.timeInMillis
        }

        suspend fun session(
            daysAgo: Int, title: String, notes: String,
            exercises: List<Triple<String, Pair<Int,Int>, Double>>
        ) {
            val id = dao.upsertWorkout(WorkoutEntity(title = title, date = dayMillis(daysAgo), notes = notes))
            dao.upsertExercises(exercises.map { (name, sr, w) ->
                ExerciseEntity(workoutId = id, name = name, sets = sr.first, reps = sr.second, weight = w)
            })
        }

        // ── Week -4: scattered days (rest on 26, 24, 20, 17) ─────────────
        session(29, "Pull Day", "Back to basics", listOf(
            Triple("Deadlift",         4 to 5,  100.0),
            Triple("Bent-over Row",    3 to 10,  60.0),
            Triple("Lat Pulldown",     3 to 12,  55.0),
            Triple("Bicep Curl",       3 to 15,  15.0)
        ))
        session(27, "Push Day", "Felt strong", listOf(
            Triple("Bench Press",      4 to 8,   80.0),
            Triple("Overhead Press",   3 to 10,  50.0),
            Triple("Incline DB Press", 3 to 12,  28.0),
            Triple("Tricep Pushdown",  3 to 15,  20.0)
        ))
        session(25, "Leg Day", "Quads on fire", listOf(
            Triple("Squat",            5 to 5,  100.0),
            Triple("Leg Press",        3 to 12, 150.0),
            Triple("Romanian DL",      3 to 10,  70.0),
            Triple("Calf Raise",       4 to 20,  60.0)
        ))
        // rest day 26, 24

        // ── Week -3: mixed, 2 rest days ───────────────────────────────────
        session(23, "Pull Day", "Good pump", listOf(
            Triple("Deadlift",         4 to 5,  102.5),
            Triple("Bent-over Row",    3 to 10,  62.5),
            Triple("Lat Pulldown",     3 to 12,  57.5),
            Triple("Face Pull",        3 to 15,  25.0),
            Triple("Bicep Curl",       3 to 15,  16.0)
        ))
        session(22, "Push Day", "Hit a new bench PR!", listOf(
            Triple("Bench Press",      5 to 5,   85.0),
            Triple("Overhead Press",   3 to 10,  52.5),
            Triple("Incline DB Press", 3 to 12,  30.0),
            Triple("Cable Flye",       3 to 15,  12.5),
            Triple("Tricep Dip",       3 to 12,   0.0)
        ))
        // rest day 21
        session(19, "Leg Day", "Slow but steady", listOf(
            Triple("Squat",            5 to 5,  102.5),
            Triple("Leg Press",        3 to 12, 155.0),
            Triple("Lunges",           3 to 12,  20.0),
            Triple("Calf Raise",       4 to 20,  62.5)
        ))
        session(18, "Full Body", "Active recovery", listOf(
            Triple("Pull-up",          3 to 8,    0.0),
            Triple("Push-up",          3 to 20,   0.0),
            Triple("Goblet Squat",     3 to 12,  24.0),
            Triple("Plank",            3 to 1,    0.0)
        ))
        // rest day 17
        session(16, "Pull Day", "Volume block", listOf(
            Triple("Deadlift",         3 to 3,  110.0),
            Triple("Bent-over Row",    4 to 10,  65.0),
            Triple("Cable Row",        3 to 12,  60.0),
            Triple("Hammer Curl",      3 to 15,  18.0)
        ))
        session(15, "Push Day", "Shoulder focus", listOf(
            Triple("Overhead Press",   5 to 5,   55.0),
            Triple("Lateral Raise",    4 to 15,   8.0),
            Triple("Front Raise",      3 to 12,   8.0),
            Triple("Bench Press",      3 to 10,  80.0),
            Triple("Tricep Extension", 3 to 15,  22.5)
        ))

        // ── 12-day streak: days 13 → 2 (no breaks) ───────────────────────
        session(13, "Push Day",  "Day 1 of streak!",         listOf(Triple("Bench Press",    4 to 8,  82.5), Triple("Overhead Press",   3 to 10, 52.5), Triple("Tricep Pushdown", 3 to 15, 22.0)))
        session(12, "Leg Day",   "Heavy squats",             listOf(Triple("Squat",          5 to 5, 105.0), Triple("Leg Press",        3 to 12,160.0), Triple("Calf Raise",      4 to 20, 65.0)))
        session(11, "Pull Day",  "Deadlift PR attempt",      listOf(Triple("Deadlift",       4 to 5, 107.5), Triple("Bent-over Row",    4 to 10, 65.0), Triple("Lat Pulldown",    3 to 12, 60.0)))
        session(10, "Push Day",  "Chest felt great",         listOf(Triple("Bench Press",    4 to 8,  85.0), Triple("Incline DB Press", 3 to 12, 32.0), Triple("Cable Flye",      3 to 15, 13.0)))
        session( 9, "Leg Day",   "Endurance session",        listOf(Triple("Squat",          5 to 5, 107.5), Triple("Romanian DL",      3 to 10, 72.5), Triple("Lunges",          3 to 12, 22.5)))
        session( 8, "Pull Day",  "Back and bis pumped",      listOf(Triple("Deadlift",       4 to 5, 110.0), Triple("Cable Row",        3 to 12, 62.5), Triple("Bicep Curl",      3 to 15, 17.0)))
        session( 7, "Full Body", "Mobility + active rest",   listOf(Triple("Pull-up",        3 to 10,  0.0), Triple("Push-up",          3 to 20,  0.0), Triple("Plank",           3 to 1,   0.0)))
        session( 6, "Push Day",  "Push for the PR!",         listOf(Triple("Bench Press",    5 to 5,  87.5), Triple("Overhead Press",   4 to 8,  55.0), Triple("Tricep Dip",      3 to 12,  0.0)))
        session( 5, "Leg Day",   "Pushed through soreness",  listOf(Triple("Squat",          5 to 5, 110.0), Triple("Leg Press",        4 to 10,165.0), Triple("Hack Squat",      3 to 12, 60.0)))
        session( 4, "Pull Day",  "Pull volume day",          listOf(Triple("Deadlift",       5 to 5, 112.5), Triple("Bent-over Row",    4 to 12, 67.5), Triple("Hammer Curl",     3 to 15, 19.0)))
        session( 3, "Push Day",  "Day 11 — almost there!",   listOf(Triple("Bench Press",    5 to 5,  90.0), Triple("Incline DB Press", 4 to 10, 34.0), Triple("Cable Flye",      3 to 15, 14.0)))
        session( 2, "Full Body", "Streak complete! 12d 🔥",  listOf(Triple("Pull-up",        4 to 10,  5.0), Triple("Push-up",          4 to 25,  0.0), Triple("Goblet Squat",    3 to 15, 28.0), Triple("Plank", 3 to 1, 0.0)))

        // rest day 1 — streak intentionally broken

        // ── Today: back at it ─────────────────────────────────────────────
        session( 0, "Push Day", "Back at it after rest 💪", listOf(
            Triple("Bench Press",      5 to 5,   92.5),
            Triple("Overhead Press",   3 to 10,  57.5),
            Triple("Incline DB Press", 3 to 12,  36.0),
            Triple("Tricep Pushdown",  3 to 15,  25.0)
        ))
    }
}
