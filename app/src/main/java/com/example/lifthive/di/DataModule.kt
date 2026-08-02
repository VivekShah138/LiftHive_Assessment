package com.example.lifthive.di

import android.content.Context
import androidx.room.Room
import com.example.lifthive.data.local.WorkoutDao
import com.example.lifthive.data.local.WorkoutDatabase
import com.example.lifthive.data.preferences.PreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideWorkoutDatabase(@ApplicationContext context: Context): WorkoutDatabase {
        return Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            WorkoutDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(db: WorkoutDatabase): WorkoutDao {
        return db.dao
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
