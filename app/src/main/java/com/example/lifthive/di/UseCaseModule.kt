package com.example.lifthive.di

import com.example.lifthive.domain.repository.WorkoutRepository
import com.example.lifthive.domain.usecase.DeleteWorkoutUseCase
import com.example.lifthive.domain.usecase.GetStatsUseCase
import com.example.lifthive.domain.usecase.GetWorkoutByIdUseCase
import com.example.lifthive.domain.usecase.GetWorkoutsUseCase
import com.example.lifthive.domain.usecase.SaveWorkoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetWorkoutsUseCase(repository: WorkoutRepository): GetWorkoutsUseCase {
        return GetWorkoutsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetWorkoutByIdUseCase(repository: WorkoutRepository): GetWorkoutByIdUseCase {
        return GetWorkoutByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveWorkoutUseCase(repository: WorkoutRepository): SaveWorkoutUseCase {
        return SaveWorkoutUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteWorkoutUseCase(repository: WorkoutRepository): DeleteWorkoutUseCase {
        return DeleteWorkoutUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetStatsUseCase(repository: WorkoutRepository): GetStatsUseCase {
        return GetStatsUseCase(repository)
    }
}
