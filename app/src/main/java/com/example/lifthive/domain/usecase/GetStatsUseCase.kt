package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.model.Workout
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

            val exerciseCounts = mutableMapOf<String, Int>()
            val totalWeightLifted = calculateTotalWeightAndCounts(workouts, exerciseCounts)

            val mostFrequentExercise = exerciseCounts.maxByOrNull { it.value }?.key ?: "None"
            val topExercises = extractTopExercises(exerciseCounts)

            val lastWorkoutsVolume = calculateLastWorkoutsVolume(workouts, sdfLabel)
            val prSession = findBestSession(workouts, sdfLabel)

            val avgVolumePerSession = if (totalWorkouts > 0) totalWeightLifted / totalWorkouts else 0.0

            val workoutDates = workouts.map { it.date }
            val workoutDayKeys = workoutDates.map { sdfDayKey.format(Date(it)) }.toSet()

            val currentStreak = calculateCurrentStreak(workoutDayKeys, sdfDayKey)
            val longestStreak = calculateLongestStreak(workoutDayKeys, sdfDayKey)

            val weeklyVolumes = calculateWeeklyVolumes(workouts)

            WorkoutStats(
                totalWorkouts = totalWorkouts,
                totalWeightLifted = totalWeightLifted,
                mostFrequentExercise = mostFrequentExercise,
                lastWorkoutsVolume = lastWorkoutsVolume,
                workoutDates = workoutDates,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                avgVolumePerSession = avgVolumePerSession,
                bestSessionVolume = prSession.first,
                bestSessionLabel = prSession.second,
                weeklyVolumes = weeklyVolumes,
                topExercises = topExercises
            )
        }
    }

    private fun calculateTotalWeightAndCounts(
        workouts: List<Workout>,
        exerciseCounts: MutableMap<String, Int>
    ): Double {
        var totalWeight = 0.0
        workouts.forEach { workout ->
            workout.exercises.forEach { exercise ->
                val volume = exercise.sets * exercise.reps * exercise.weight
                totalWeight += volume
                val name = exercise.name.trim()
                if (name.isNotEmpty()) {
                    exerciseCounts[name] = exerciseCounts.getOrDefault(name, 0) + 1
                }
            }
        }
        return totalWeight
    }

    private fun extractTopExercises(exerciseCounts: Map<String, Int>): List<Pair<String, Int>> {
        return exerciseCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { Pair(it.key, it.value) }
    }

    private fun calculateLastWorkoutsVolume(
        workouts: List<Workout>,
        sdfLabel: SimpleDateFormat
    ): List<Pair<String, Double>> {
        val last6Workouts = workouts.take(6).reversed()
        return last6Workouts.map { workout ->
            val volume = workout.exercises.sumOf { it.sets * it.reps * it.weight }
            val title = sdfLabel.format(Date(workout.date))
            Pair(title, volume)
        }
    }

    private fun findBestSession(
        workouts: List<Workout>,
        sdfLabel: SimpleDateFormat
    ): Pair<Double, String> {
        var bestSessionVolume = 0.0
        var bestSessionLabel = "—"
        workouts.forEach { workout ->
            val vol = workout.exercises.sumOf { it.sets * it.reps * it.weight }
            if (vol > bestSessionVolume) {
                bestSessionVolume = vol
                bestSessionLabel = sdfLabel.format(Date(workout.date))
            }
        }
        return Pair(bestSessionVolume, bestSessionLabel)
    }

    private fun calculateCurrentStreak(
        workoutDayKeys: Set<String>,
        sdfDayKey: SimpleDateFormat
    ): Int {
        var streak = 0
        val cal = Calendar.getInstance()
        val todayKey = sdfDayKey.format(cal.time)
        val startKey = if (workoutDayKeys.contains(todayKey)) todayKey
        else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            sdfDayKey.format(cal.time)
        }
        cal.time = sdfDayKey.parse(startKey) ?: cal.time
        while (workoutDayKeys.contains(sdfDayKey.format(cal.time))) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun calculateLongestStreak(
        workoutDayKeys: Set<String>,
        sdfDayKey: SimpleDateFormat
    ): Int {
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
        return longestStreak
    }

    private fun calculateWeeklyVolumes(workouts: List<Workout>): List<Pair<String, Double>> {
        val weeklyVolumes = mutableListOf<Pair<String, Double>>()
        val weekCal = Calendar.getInstance()
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
            val weekLabel = if (w == 0) {
                "This Week"
            } else {
                val startFmt = SimpleDateFormat("MMM d", Locale.getDefault())
                val endFmt   = SimpleDateFormat("MMM d", Locale.getDefault())
                val endDay   = Calendar.getInstance().apply {
                    timeInMillis = weekEnd.timeInMillis
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                "${startFmt.format(weekStart.time)} – ${endFmt.format(endDay.time)}"
            }
            val weekVol = workouts
                .filter { it.date >= weekStart.timeInMillis && it.date < weekEnd.timeInMillis }
                .sumOf { w2 -> w2.exercises.sumOf { it.sets * it.reps * it.weight } }
            weeklyVolumes.add(Pair(weekLabel, weekVol))
        }
        return weeklyVolumes
    }
}
