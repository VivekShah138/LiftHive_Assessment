package com.example.lifthive.domain.usecase

import com.example.lifthive.domain.repository.PreferencesRepository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(isDark: Boolean) {
        repository.setTheme(isDark)
    }
}
