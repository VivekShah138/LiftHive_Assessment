package com.example.lifthive.data.repository

import com.example.lifthive.data.local.preferences.PreferencesDataStore
import com.example.lifthive.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: PreferencesDataStore
) : PreferencesRepository {

    override val isDarkTheme: Flow<Boolean> = dataStore.isDarkTheme

    override suspend fun setTheme(isDark: Boolean) {
        dataStore.setTheme(isDark)
    }
}
