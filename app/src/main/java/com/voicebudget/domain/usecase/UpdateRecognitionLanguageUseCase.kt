package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateRecognitionLanguageUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(languageTag: String) = repository.setRecognitionLanguageTag(languageTag)
}
