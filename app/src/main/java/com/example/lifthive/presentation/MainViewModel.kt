package com.example.lifthive.presentation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val prefs = application.getSharedPreferences("lifthive_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkTheme = mutableStateOf(prefs.getBoolean("is_dark_theme", true)) // Default to dark theme for premium fitness aesthetic
    val isDarkTheme: State<Boolean> = _isDarkTheme

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        prefs.edit().putBoolean("is_dark_theme", _isDarkTheme.value).apply()
    }
}
