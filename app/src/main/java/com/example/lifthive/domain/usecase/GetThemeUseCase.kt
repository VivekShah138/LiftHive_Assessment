package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isDarkTheme
    }
}
