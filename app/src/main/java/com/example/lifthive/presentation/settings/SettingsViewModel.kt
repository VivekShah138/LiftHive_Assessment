package com.example.lifthive.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifthive.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    object PopulateDummyData : SettingsEvent
    object ClearAllData : SettingsEvent
}

sealed interface SettingsUiEffect {
    object DummyDataPopulated : SettingsUiEffect
    object DataCleared : SettingsUiEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _effect = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val effect: Flow<SettingsUiEffect> = _effect.receiveAsFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.PopulateDummyData -> {
                viewModelScope.launch {
                    repository.populateDummyData()
                    _effect.send(SettingsUiEffect.DummyDataPopulated)
                }
            }
            is SettingsEvent.ClearAllData -> {
                viewModelScope.launch {
                    repository.clearAllWorkouts()
                    _effect.send(SettingsUiEffect.DataCleared)
                }
            }
        }
    }
}
