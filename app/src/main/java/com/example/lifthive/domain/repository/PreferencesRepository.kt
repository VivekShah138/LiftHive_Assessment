package com.example.lifthive.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val isDarkTheme: Flow<Boolean>
    suspend fun setTheme(isDark: Boolean)
}
