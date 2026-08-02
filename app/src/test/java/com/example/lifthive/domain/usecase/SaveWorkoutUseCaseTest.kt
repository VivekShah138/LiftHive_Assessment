package com.example.lifthive.domain.usecase

import com.example.lifthive.FakeWorkoutRepository
import com.example.lifthive.domain.model.Exercise
import com.example.lifthive.domain.model.Workout
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveWorkoutUseCaseTest {

    private lateinit var saveWorkoutUseCase: SaveWorkoutUseCase
    private lateinit var repository: FakeWorkoutRepository

    @Before
    fun setUp() {
        repository = FakeWorkoutRepository()
        saveWorkoutUseCase = SaveWorkoutUseCase(repository)
    }

    @Test
    fun saveWorkoutWithEmptyTitleThrowsException() {
        val workout = Workout(
            title = "",
            date = 12345L,
            notes = "Notes",
            exercises = listOf(Exercise(name = "Squats", sets = 3, reps = 10, weight = 100.0))
        )

        val exception = assertThrows(SaveWorkoutUseCase.InvalidWorkoutException::class.java) {
            runBlocking { saveWorkoutUseCase(workout) }
        }
        assertEquals("Workout title cannot be empty.", exception.message)
    }

    @Test
    fun saveWorkoutWithEmptyExercisesThrowsException() {
        val workout = Workout(
            title = "Leg Day",
            date = 12345L,
            notes = "Notes",
            exercises = emptyList()
        )

        val exception = assertThrows(SaveWorkoutUseCase.InvalidWorkoutException::class.java) {
            runBlocking { saveWorkoutUseCase(workout) }
        }
        assertEquals("A workout must contain at least one exercise.", exception.message)
    }

    @Test
    fun saveWorkoutWithInvalidSetsThrowsException() {
        val workout = Workout(
            title = "Leg Day",
            date = 12345L,
            notes = "Notes",
            exercises = listOf(
                Exercise(name = "Squats", sets = 0, reps = 10, weight = 100.0)
            )
        )

        val exception = assertThrows(SaveWorkoutUseCase.InvalidWorkoutException::class.java) {
            runBlocking { saveWorkoutUseCase(workout) }
        }
        assertEquals("Sets must be greater than zero for Squats.", exception.message)
    }

    @Test
    fun saveWorkoutWithValidInputsSuccess() = runBlocking {
        val workout = Workout(
            title = "Leg Day",
            date = 12345L,
            notes = "Notes",
            exercises = listOf(
                Exercise(name = "Squats", sets = 3, reps = 10, weight = 100.0)
            )
        )

        val resultId = saveWorkoutUseCase(workout)
        assertEquals(1L, resultId)
        
        val saved = repository.getWorkoutById(1L)
        assertTrue(saved != null)
        assertEquals("Leg Day", saved?.title)
    }
}
