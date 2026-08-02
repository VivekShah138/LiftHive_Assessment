package com.example.lifthive.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifthive.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    fun populateDummyData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.populateDummyData()
            onSuccess()
        }
    }

    fun clearAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllWorkouts()
            onSuccess()
        }
    }
}
