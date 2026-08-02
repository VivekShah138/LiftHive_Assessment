package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.WorkoutStats
import com.example.lifthive.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<WorkoutStats> {
        return repository.getWorkouts().map { workouts ->
            val totalWorkouts = workouts.size
            val sdfLabel = SimpleDateFormat("MMM d", Locale.getDefault())
            val sdfDayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

            // Top 5 exercises by frequency
            val topExercises = exerciseCounts.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { Pair(it.key, it.value) }

            // Last 6 sessions volume with date labels (oldest → newest)
            val last6Workouts = workouts.take(6).reversed()
            val lastWorkoutsVolume = last6Workouts.map { workout ->
                val volume = workout.exercises.sumOf { it.sets * it.reps * it.weight }
                val title = sdfLabel.format(Date(workout.date))
                Pair(title, volume)
            }

            // All-time best single-session PR
            var bestSessionVolume = 0.0
            var bestSessionLabel = "—"
            workouts.forEach { workout ->
                val vol = workout.exercises.sumOf { it.sets * it.reps * it.weight }
                if (vol > bestSessionVolume) {
                    bestSessionVolume = vol
                    bestSessionLabel = sdfLabel.format(Date(workout.date))
                }
            }

            // Average volume per session
            val avgVolumePerSession = if (totalWorkouts > 0) totalWeightLifted / totalWorkouts else 0.0

            // Workout dates as Set<"yyyy-MM-dd">
            val workoutDates = workouts.map { it.date }
            val workoutDayKeys = workoutDates.map { sdfDayKey.format(Date(it)) }.toSet()

            // Current streak (consecutive days ending today or yesterday)
            var currentStreak = 0
            val cal = Calendar.getInstance()
            val todayKey = sdfDayKey.format(cal.time)
            // Start from today; if today has no workout, allow yesterday as start
            val startKey = if (workoutDayKeys.contains(todayKey)) todayKey
            else {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                sdfDayKey.format(cal.time)
            }
            // Reset cal to startKey date
            cal.time = sdfDayKey.parse(startKey) ?: cal.time
            while (workoutDayKeys.contains(sdfDayKey.format(cal.time))) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }

            // Longest streak (all-time)
            val sortedDayKeys = workoutDayKeys.mapNotNull { sdfDayKey.parse(it) }
                .map { it.time }
                .sorted()
            var longestStreak = 0
            var runStreak = 0
            var prevCal: Calendar? = null
            sortedDayKeys.forEach { millis ->
                val curr = Calendar.getInstance().apply { timeInMillis = millis }
                if (prevCal == null) {
                    runStreak = 1
                } else {
                    val prev = prevCal!!
                    prev.add(Calendar.DAY_OF_YEAR, 1)
                    val expectedKey = sdfDayKey.format(prev.time)
                    val currKey = sdfDayKey.format(curr.time)
                    runStreak = if (expectedKey == currKey) runStreak + 1 else 1
                }
                if (runStreak > longestStreak) longestStreak = runStreak
                prevCal = Calendar.getInstance().apply { timeInMillis = millis }
            }

            // Weekly volumes for the last 4 complete weeks
            val weeklyVolumes = mutableListOf<Pair<String, Double>>()
            val weekCal = Calendar.getInstance()
            // Snap to start of this week (Sunday)
            weekCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            weekCal.set(Calendar.HOUR_OF_DAY, 0)
            weekCal.set(Calendar.MINUTE, 0)
            weekCal.set(Calendar.SECOND, 0)
            weekCal.set(Calendar.MILLISECOND, 0)

            for (w in 3 downTo 0) {
                val weekStart = Calendar.getInstance().apply {
                    timeInMillis = weekCal.timeInMillis
                    add(Calendar.WEEK_OF_YEAR, -w)
                }
                val weekEnd = Calendar.getInstance().apply {
                    timeInMillis = weekStart.timeInMillis
                    add(Calendar.DAY_OF_YEAR, 7)
                }
                val weekLabel = if (w == 0) "This week" else "-${w}w"
                val weekVol = workouts
                    .filter { it.date >= weekStart.timeInMillis && it.date < weekEnd.timeInMillis }
                    .sumOf { w2 -> w2.exercises.sumOf { it.sets * it.reps * it.weight } }
                weeklyVolumes.add(Pair(weekLabel, weekVol))
            }

            WorkoutStats(
                totalWorkouts = totalWorkouts,
                totalWeightLifted = totalWeightLifted,
                mostFrequentExercise = mostFrequentExercise,
                lastWorkoutsVolume = lastWorkoutsVolume,
                workoutDates = workoutDates,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                avgVolumePerSession = avgVolumePerSession,
                bestSessionVolume = bestSessionVolume,
                bestSessionLabel = bestSessionLabel,
                weeklyVolumes = weeklyVolumes,
                topExercises = topExercises
            )
        }
    }
}
