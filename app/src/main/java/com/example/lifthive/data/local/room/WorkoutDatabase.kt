package com.example.lifthive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lifthive.data.local.entities.ExerciseEntity
import com.example.lifthive.data.local.entities.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class, ExerciseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract val dao: WorkoutDao

    companion object {
        const val DATABASE_NAME = "lifthive_workout_db"
    }
}
