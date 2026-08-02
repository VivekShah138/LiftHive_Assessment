package com.example.lifthive.domain.usecase

import com.example.lifthive.FakeWorkoutRepository
import com.example.lifthive.domain.model.Exercise
import com.example.lifthive.domain.model.Workout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetStatsUseCaseTest {

    private lateinit var getStatsUseCase: GetStatsUseCase
    private lateinit var repository: FakeWorkoutRepository

    @Before
    fun setUp() {
        repository = FakeWorkoutRepository()
        getStatsUseCase = GetStatsUseCase(repository)
    }

    @Test
    fun emptyRepositoryReturnsDefaultStats() = runBlocking {
        val stats = getStatsUseCase().first()
        assertEquals(0, stats.totalWorkouts)
        assertEquals(0.0, stats.totalWeightLifted, 0.001)
        assertEquals("None", stats.mostFrequentExercise)
        assertEquals(0, stats.lastWorkoutsVolume.size)
    }

    @Test
    fun repositoryWithWorkoutsReturnsCorrectStatistics() = runBlocking {
        // Session 1: Push Day
        val w1 = Workout(
            id = 1L,
            title = "Push Day",
            date = 1000L,
            notes = "",
            exercises = listOf(
                Exercise(name = "Bench Press", sets = 3, reps = 10, weight = 80.0), // 2400
                Exercise(name = "Shoulder Press", sets = 3, reps = 8, weight = 20.0) // 480
            )
        )
        // Session 2: Legs Day
        val w2 = Workout(
            id = 2L,
            title = "Legs Day",
            date = 2000L,
            notes = "",
            exercises = listOf(
                Exercise(name = "Squats", sets = 4, reps = 5, weight = 100.0), // 2000
                // Bench Press again
                Exercise(name = "Bench Press", sets = 3, reps = 10, weight = 80.0) // 2400
            )
        )

        repository.saveWorkout(w1)
        repository.saveWorkout(w2)

        val stats = getStatsUseCase().first()
        
        // Total workouts
        assertEquals(2, stats.totalWorkouts)
        
        // Total Volume = 2400 + 480 + 2000 + 2400 = 7280
        assertEquals(7280.0, stats.totalWeightLifted, 0.001)
        
        // Most frequent exercise (Bench Press occurs twice, others once)
        assertEquals("Bench Press", stats.mostFrequentExercise)
        
        // Last workouts volume (2 workouts, sorted chronologically: oldest w1 then newest w2)
        assertEquals(2, stats.lastWorkoutsVolume.size)
        
        // Check w1 volume
        assertEquals("Push Day", stats.lastWorkoutsVolume[0].first)
        assertEquals(2880.0, stats.lastWorkoutsVolume[0].second, 0.001)
        
        // Check w2 volume
        assertEquals("Legs Day", stats.lastWorkoutsVolume[1].first)
        assertEquals(4400.0, stats.lastWorkoutsVolume[1].second, 0.001)
    }
}
