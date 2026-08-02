package com.example.lifthive.data.local

import android.content.Context
import com.example.lifthive.data.local.entities.ExerciseEntity
import com.example.lifthive.data.local.entities.WorkoutEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds realistic sample workout data on first app launch.
 *
 * Schedule (relative to today, going backwards):
 *  ┌─────────────────────────────────────────────────────────────────┐
 *  │  Week -3  │  Mon Tue Wed     Fri Sat Sun   ← 6 days            │
 *  │  Week -2  │  Mon Tue Wed Thu Fri           ← 5 days            │
 *  │  Week -1  │  Mon Tue Wed Thu Fri Sat       ← streak continues  │
 *  │  Week  0  │  Mon Tue Wed Thu Fri Sat Sun   ← 12-day streak end │
 *  │  (gap)    │  Tue Wed  skipped              ← streak broken      │
 *  │  Week  0  │  ... Thu Fri Sat Sun (today or yesterday)           │
 *  └─────────────────────────────────────────────────────────────────┘
 *
 * Concretely the seeder builds a set of offsets from today.
 * Negative offset = N days ago.  Positive = future (not used).
 *
 * Longest streak = 12 consecutive days  (offsets -13 .. -2)
 * Current streak = depends on today's workout (seeded if today is included)
 * Broken days    = offsets -26, -24, -20, -17 (rest days scattered in week -3 / -4)
 */
@Singleton
class SeedDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: WorkoutDao
) {
    companion object {
        private const val PREFS_NAME = "lifthive_prefs"
        private const val KEY_SEEDED = "sample_data_seeded_v2"
    }

    fun seedIfNeeded() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            insertSampleData()
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
    }

    private suspend fun insertSampleData() {
        // Build calendar anchored to start of today
        fun dayMillis(daysAgo: Int): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            cal.set(Calendar.HOUR_OF_DAY, 8)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        // ── Workout sessions (offset = days ago) ──────────────────────────
        // Scattered sessions in week -4 and -3 (some rest days deliberately)
        val scatteredDays = listOf(29, 27, 25, 23, 22, 19, 18, 16, 15)
        // 12-day streak: days -13 through -2 (inclusive) = 12 days
        val streakDays = (2..13).toList()   // 13 days ago … 2 days ago
        // Today (offset 0) — user just logged today
        val todayDay = listOf(0)

        data class SessionSpec(
            val daysAgo: Int,
            val title: String,
            val notes: String,
            val exercises: List<Triple<String, Pair<Int, Int>, Double>>  // name, (sets,reps), weight
        )

        val sessions = buildList {
            // ── Scattered pre-streak sessions ─────────────────────────────
            add(SessionSpec(29, "Pull Day", "Back to basics", listOf(
                Triple("Deadlift",          4 to 5,  100.0),
                Triple("Bent-over Row",     3 to 10,  60.0),
                Triple("Lat Pulldown",      3 to 12,  55.0),
                Triple("Bicep Curl",        3 to 15,  15.0)
            )))
            add(SessionSpec(27, "Push Day", "Felt strong today", listOf(
                Triple("Bench Press",       4 to 8,   80.0),
                Triple("Overhead Press",    3 to 10,  50.0),
                Triple("Incline Dumbbell",  3 to 12,  28.0),
                Triple("Tricep Pushdown",   3 to 15,  20.0)
            )))
            add(SessionSpec(25, "Leg Day", "Quads are on fire", listOf(
                Triple("Squat",             5 to 5,   100.0),
                Triple("Leg Press",         3 to 12,  150.0),
                Triple("Romanian Deadlift", 3 to 10,   70.0),
                Triple("Calf Raise",        4 to 20,   60.0)
            )))
            add(SessionSpec(23, "Pull Day", "Good pump", listOf(
                Triple("Deadlift",          4 to 5,  102.5),
                Triple("Bent-over Row",     3 to 10,  62.5),
                Triple("Lat Pulldown",      3 to 12,  57.5),
                Triple("Face Pull",         3 to 15,  25.0),
                Triple("Bicep Curl",        3 to 15,  16.0)
            )))
            add(SessionSpec(22, "Push Day", "Hit a new bench PR!", listOf(
                Triple("Bench Press",       5 to 5,   85.0),
                Triple("Overhead Press",    3 to 10,  52.5),
                Triple("Incline Dumbbell",  3 to 12,  30.0),
                Triple("Cable Flye",        3 to 15,  12.5),
                Triple("Tricep Dip",        3 to 12,   0.0)
            )))
            // gap day 21 — rest
            add(SessionSpec(19, "Leg Day", "Slow but steady", listOf(
                Triple("Squat",             5 to 5,  102.5),
                Triple("Leg Press",         3 to 12, 155.0),
                Triple("Lunges",            3 to 12,  20.0),
                Triple("Calf Raise",        4 to 20,  62.5)
            )))
            add(SessionSpec(18, "Full Body", "Active recovery", listOf(
                Triple("Pull-up",           3 to  8,   0.0),
                Triple("Push-up",           3 to 20,   0.0),
                Triple("Goblet Squat",      3 to 12,  24.0),
                Triple("Plank",             3 to  1,   0.0)
            )))
            // gap day 17 — rest
            add(SessionSpec(16, "Pull Day", "Volume block", listOf(
                Triple("Deadlift",          3 to 3,  110.0),
                Triple("Bent-over Row",     4 to 10,  65.0),
                Triple("Cable Row",         3 to 12,  60.0),
                Triple("Hammer Curl",       3 to 15,  18.0)
            )))
            add(SessionSpec(15, "Push Day", "Shoulder focus", listOf(
                Triple("Overhead Press",    5 to 5,   55.0),
                Triple("Lateral Raise",     4 to 15,   8.0),
                Triple("Front Raise",       3 to 12,   8.0),
                Triple("Bench Press",       3 to 10,  80.0),
                Triple("Tricep Extension",  3 to 15,  22.5)
            )))

            // ── 12-day consecutive streak (days 13 → 2) ───────────────────
            val streakTitles = listOf(
                "Push Day", "Leg Day", "Pull Day", "Push Day",
                "Leg Day",  "Pull Day","Full Body", "Push Day",
                "Leg Day",  "Pull Day","Push Day",  "Full Body"
            )
            val streakNotes = listOf(
                "Killing it — day 1 of streak!",
                "Heavy squats today",
                "Deadlift PR attempt",
                "Chest felt great",
                "Leg endurance session",
                "Back and bis pumped",
                "Active recovery / mobility",
                "Push for the PR!",
                "Quad soreness but pushed through",
                "Pull volume day",
                "Day 11 — almost there!",
                "Streak complete! 12 days! 🔥"
            )
            val streakExercises: List<List<Triple<String, Pair<Int, Int>, Double>>> = listOf(
                listOf(Triple("Bench Press",4 to 8,82.5), Triple("Overhead Press",3 to 10,52.5), Triple("Tricep Pushdown",3 to 15,22.0)),
                listOf(Triple("Squat",5 to 5,105.0),       Triple("Leg Press",3 to 12,160.0),    Triple("Calf Raise",4 to 20,65.0)),
                listOf(Triple("Deadlift",4 to 5,107.5),    Triple("Bent-over Row",4 to 10,65.0), Triple("Lat Pulldown",3 to 12,60.0)),
                listOf(Triple("Bench Press",4 to 8,85.0),  Triple("Incline DB Press",3 to 12,32.0),Triple("Cable Flye",3 to 15,13.0)),
                listOf(Triple("Squat",5 to 5,107.5),       Triple("Romanian DL",3 to 10,72.5),   Triple("Lunges",3 to 12,22.5)),
                listOf(Triple("Deadlift",4 to 5,110.0),    Triple("Cable Row",3 to 12,62.5),     Triple("Bicep Curl",3 to 15,17.0)),
                listOf(Triple("Pull-up",3 to 10,0.0),      Triple("Push-up",3 to 20,0.0),        Triple("Plank",3 to 1,0.0)),
                listOf(Triple("Bench Press",5 to 5,87.5),  Triple("Overhead Press",4 to 8,55.0), Triple("Tricep Dip",3 to 12,0.0)),
                listOf(Triple("Squat",5 to 5,110.0),       Triple("Leg Press",4 to 10,165.0),    Triple("Hack Squat",3 to 12,60.0)),
                listOf(Triple("Deadlift",5 to 5,112.5),    Triple("Bent-over Row",4 to 12,67.5), Triple("Hammer Curl",3 to 15,19.0)),
                listOf(Triple("Bench Press",5 to 5,90.0),  Triple("Incline DB Press",4 to 10,34.0),Triple("Cable Flye",3 to 15,14.0)),
                listOf(Triple("Pull-up",4 to 10,5.0),      Triple("Push-up",4 to 25,0.0),        Triple("Goblet Squat",3 to 15,28.0), Triple("Plank",3 to 1,0.0))
            )
            streakDays.forEachIndexed { i, daysAgo ->
                add(SessionSpec(daysAgo, streakTitles[i], streakNotes[i], streakExercises[i]))
            }

            // ── gap days 1 — rest (yesterday absent, streak ended)
            // Today's session
            add(SessionSpec(0, "Push Day", "Back at it after rest day 💪", listOf(
                Triple("Bench Press",       5 to 5,   92.5),
                Triple("Overhead Press",    3 to 10,  57.5),
                Triple("Incline DB Press",  3 to 12,  36.0),
                Triple("Tricep Pushdown",   3 to 15,  25.0)
            )))
        }

        // ── Insert into Room ───────────────────────────────────────────────
        sessions.forEach { spec ->
            val workoutId = dao.insertWorkout(
                WorkoutEntity(
                    title = spec.title,
                    date  = dayMillis(spec.daysAgo),
                    notes = spec.notes
                )
            )
            val exerciseEntities = spec.exercises.map { (name, setsReps, weight) ->
                ExerciseEntity(
                    workoutId = workoutId,
                    name      = name,
                    sets      = setsReps.first,
                    reps      = setsReps.second,
                    weight    = weight
                )
            }
            dao.insertExercises(exerciseEntities)
        }
    }
}
